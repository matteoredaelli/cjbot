(ns cjbot.core
  (:gen-class)
  (:use 
        [cjbot.mq]
        [clojure.tools.cli :only (cli)]
        )
  (:require [clojure.data.json :as json]
            [taoensso.timbre :as timbre
             :refer (trace debug info warn error fatal spy with-log-level)])
  )


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
   
    (timbre/set-level! :warn)
    (case (:cmd opts)
      "todo" (println (:cmd opts))
      ;; else      
      (println "running...")
      )))
