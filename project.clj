(defproject tranquil "0.1.0-SNAPSHOT"
  :description "A CLI tool that generates poems"
  :url "https://github.com/malekoa/tranquil"
  :license {:name "MIT"
            :url "https://opensource.org/license/mit"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [net.clojars.wkok/openai-clojure "0.22.0"]
                 [org.clojure/data.json "2.5.1"]
                 [org.jline/jline "3.23.0"]
                 [com.taoensso/telemere "1.0.0-beta25"]]
  :main ^:skip-aot tranquil.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
