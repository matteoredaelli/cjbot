(ns cjbot.core
  (:gen-class)
  (:use [cjbot.sources.twitter]
        [clojure.tools.cli :only (cli)]
        ))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [[opts args banner]
        (cli args
             ["-h" "--help" "Show help" :flag true :default false]
             ["-d" "--depth" "depth" :default "0"]
             ["-s" "--sleep" "sleep" :default "10000"]
             ["-u" "--user-id" "user-id like 16837493"]
             )]
    (when (or (:help opts) (not (:user-id opts)))
      (println banner)
      (System/exit 0))
    (crawl-twitter-users :depth (read-string (:depth opts))
                         :user-id (read-string (:user-id opts))
                         :crawler-params {;; TODO :crawl-friends true 
                                          ;; TODO :crawl-followers true 
                                          ;; TODO :crawl-urls false
                                          :crawled-users #{}
                                          :sleep (read-string (:sleep opts))
                                          ;; TODO :redis-server {:server "localhost" :port 1111}
                                          ;; TODO :amqp-server {:server "localhost" :port 1111 :queue "cjbot.results"}
                                          ;; TODO :riak-server {:server "localhost" :port 1111}
                                        }
                       )))
