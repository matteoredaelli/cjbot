(ns cjbot.core
  (:gen-class)
  (:use 
        [cjbot.crawler]
        [clojure.tools.cli :only (cli)]
        )
  (:require [clojure.data.json :as json]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [[opts args banner]
        (cli args
             ["-c" "--cmd" "lookup-users|crawl-users|lists-members"]
             ["-h" "--help" "Show help" :flag true :default false]
             )]
    (when (or (:help opts)
              (not (:cmd opts)))
      (println banner)
      (System/exit 0))
    (case (:cmd opts)
      "todo" (println (:cmd opts))
      ;; else      
      (println banner)
      )))
