(ns cjbot.core
  (:gen-class)
  (:use [cjbot.sources.twitter]
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
             ["-d" "--depth" "depth" :default "0"]
             ["-l" "--list-id" "list-id like 21931460"]
             ["-o" "--output" "output to stdout|redis" :default "stdout"]
             ["-s" "--sleep" "sleep" :default "10000"]
             ["-t" "--timelines" "retreive timelines" :flag true :default true]
             ["-u" "--user-id" "user-id like 16837493"]
             ["-N" "--screen-name" "username like matteoredaelli"]
             )]
    (when (or (:help opts)
              (not (:cmd opts)))
      (println banner)
      (System/exit 0))
    (case (:cmd opts)
      "lists-members" (if (:list-id opts)
                        (let [members (all-lists-members (read-string (:list-id opts)))]
                          (when (re-find #"stdout" (:output opts))
                            (doseq [m members]
                              (println (json/write-str m))))))

      "lookup-users" (if (:user-id opts)
                       (get-users-lookup {:user-id (read-string (:user-id opts))})
                       (get-users-lookup {:screen-name (read-string (:screen-name opts))}))
      "crawl-users"  
      (crawl-twitter-users :depth (read-string (:depth opts))
                           :user-id (read-string (:user-id opts))
                           :crawler-params {;; TODO :crawl-friends true 
                                            ;; TODO :crawl-followers true 
                                            ;; TODO :crawl-urls false
                                            :crawled-users #{}
                                            :sleep (read-string (:sleep opts))
                                            :output (:output opts)
                                            :timelines (:timelines opts)
                                            ;; TODO :redis-server {:server "localhost" :port 1111}
                                            ;; TODO :amqp-server {:server "localhost" :port 1111 :queue "cjbot.results"}
                                            ;; TODO :riak-server {:server "localhost" :port 1111}
                                            }
                           )
      (println banner)
      )))
