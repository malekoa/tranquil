(ns tranquil.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [wkok.openai-clojure.api :as openai]
            [taoensso.telemere :as t])
  (:gen-class))


;; CLI options
(def cli-options
  [["-h" "--help" "Show help"]
   ["-v" "--version" "Show version"]
   ["-t" "--type TYPE" "Specify the type of poem (e.g., haiku, sonnet, limerick)" :required true]
   ["-p" "--prompt PROMPT" "What the poem should be about (optional)."]])

(defn usage []
  (let [formatted-options (string/join "\n"
                                       (map (fn [[short long desc]]
                                              (format "  %-4s %-16s %s"
                                                      (or short "") (or long "") desc))
                                            cli-options))]
    (str "Usage: tranquil [argument] [options]\n\n"
         "Arguments:\n"
         "  poem             Generate a poem of the specified type.\n\n"
         "Options:\n"
         formatted-options)))


(def config-file-path
  (str (System/getProperty "user.home") "/.config/tranquil/config.json"))

(defn save-config
  "Save the configuration map to the file system."
  [config]
  (spit config-file-path (json/write-str config))
  (println "Configuration saved to" config-file-path))

(defn load-config
  "Load the configuration from the file system as a map."
  []
  (when (.exists (io/file config-file-path))
    (json/read-str (slurp config-file-path) :key-fn keyword)))

(defn hidden-prompt [prompt]
  (if-let [console (System/console)]
    ;; read the password without echo using console
    (String. (.readPassword console "%s" (into-array Object [prompt])))
    ;; fallback to standard input if console not available
    (do
      (println "Console is not available. Falling back to standard input (input will be visible).")
      (print prompt)
      (flush)
      (read-line))))

(defn check-openai-api-key [api-key]
  (try
    (let [_ (openai/list-models {:api-key api-key})]
      true)
    (catch clojure.lang.ExceptionInfo e
      (let [status (-> e ex-data :status)]
        (case status
          401 false ;; 401 unauthorized, the key itself is not valid
          429 (do (println "Rate limit exceeded. Please try again later.") false) ;; Key may be valid, but rate limit is exceeded
          (do (println "Unexpected error:" (.getMessage e)) false))))
    (catch Exception e
      (println "Network or other error occurred:" (.getMessage e))
      false)))


(defn key-invalid
  "Prompt for a new API key when the current one is invalid."
  []
  (loop []
    (let [new-api-key (hidden-prompt "Enter OpenAI API key: ")]
      (if (check-openai-api-key new-api-key)
        (do
          (save-config {:openai_api_key new-api-key})
          new-api-key)
        (do
          (println "Invalid API key. Try again.")
          (recur))))))

(defn create-config
  "Prompt the user to create a new configuration file with a valid API key."
  []
  (println "No config file detected. Creating one now.")
  (loop []
    (let [api-key (hidden-prompt "Enter OpenAI API key: ")]
      (if (check-openai-api-key api-key)
        (do
          (save-config {:openai_api_key api-key})
          api-key)
        (do
          (println "Invalid API key. Try again.")
          (recur))))))

(defn get-api-key
  "Retrieve the OpenAI API key from the configuration, creating or updating it if necessary."
  []
  (if-let [config (load-config)]
    (let [api-key (:openai_api_key config)]
      (if (check-openai-api-key api-key)
        api-key
        (do
          (println "Current OpenAI API key is invalid. Please enter a new API key.")
          (key-invalid))))
    (create-config)))

(def poem-types ["haiku" "sonnet" "limerick" "tanka" "cinquain" "epigram" "triolet" "monostich"])

(defn poem
  "Generate a poem of the specified type and prompt."
  [type prompt]
  (let [api-key (get-api-key)
        base-prompt (case type
                      "haiku" "Write a haiku (A 3-line poem with a 5-7-5 syllable structure)"
                      "sonnet" "Write a sonnet (A 14-line poem with a set rhyme scheme, typically exploring themes of love, beauty, or philosophy)"
                      "limerick" "Write a limerick (A humorous, five-line poem with an AABBA rhyme scheme)"
                      "tanka" "Write a tanka (A 5-line poem with a 5-7-5-7-7 syllable pattern)"
                      "cinquain" "Write a cinquain (A 5-line poem with a 2-4-6-8-2 syllable structure)"
                      "epigram" "Write an epigram (A witty, often satirical, short poem with a punchline)"
                      "triolet" "Write a triolet (An 8-line poem where the 1st, 4th, and 7th lines are identical, and the 2nd and 8th lines repeat)"
                      "monostich" "Write a monostich (A single-line poem)"
                      (throw (IllegalArgumentException. (str "Invalid type: " type ". Must be one of: " poem-types))))
        final-prompt (if prompt
                       (str base-prompt " about " prompt ".")
                       (str base-prompt " on any topic."))
        response (openai/create-chat-completion
                  {:model "gpt-4o-mini"
                   :messages [{:role "system" :content "You are a world-renowned poet, skilled in various forms of poetry."}
                              {:role "user" :content final-prompt}]}
                  {:api-key api-key})]
    (println (((first (:choices response)) :message) :content))))


(defn -main [& args]
  (t/call-on-shutdown! t/stop-handlers!) ;; This calls t/stop-handlers! to flush their buffers before the jvm shuts down.
  (let [{:keys [options arguments errors]}
        (parse-opts args cli-options)
        type (:type options)
        prompt (:prompt options)]
    (cond
      (:help options) (println (usage))
      (:version options) (println "tranquil - version 0.0.1")
      (not= 1 (count arguments)) (do
                                   (t/log! :error "Incorrect number of arguments.")
                                   (println "Error: incorrect number of arguments. Use --help for usage.")
                                   (System/exit 1))
      (nil? type) (do
                    (println "Error: --type is required. Use --help for usage.")
                    (System/exit 1))
      errors (do (println "Errors:" errors) (System/exit 1))
      :else (poem type prompt))))
