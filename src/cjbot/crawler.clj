(ns cjbot.crawler
  (:gen-class)
  (:use 
   [cjbot.redis]
   ;; sources
   [cjbot.sources.twitter])
  (:require 
   [clojure.data.json :as json]
   [taoensso.carmine :as car :refer (wcar)]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))


;; input and output must be map
(defn cjbot-reqs-eval [req]
  (let [source (req :source)
        body (req :body)
        key (body :key)
        value (body :value)
        function-string (format "%s-%s" source key)]
    (warn "Calling function " function-string "with param" value)
    (def resp (try (@(resolve(symbol function-string)) value)
                   (catch Exception e (str "caught exception: " (.getMessage e)))))
    (warn "Called function " function-string "with output=" resp)
    (debug "  with output=" resp)
    resp))

;; input and output must be json
(defn cjbot-reqs-listener [msg]
  (println "Pattern match: " msg)
  (case (first msg) 
    "message" (let [req (last msg)
                    req-map (decode-message req)
                    resp (cjbot-reqs-eval req-map)
                    resp-json (encode-message {:req req :resp resp})]
                ;;(wcar* (car/publish "cjbot.crawler.resp" resp-json))
                (warn "Result=" resp))
    (println "no action")))

(def listener-crawler
  (let [queue-crawler-req (cjbot-queue-name-with-prefix "crawler.req")
        queues-crawler (cjbot-queue-name-with-prefix "crawler*")]
    (car/with-new-pubsub-listener (:spec redis-conn)
      {queue-crawler-req (fn f1 [msg] (cjbot-reqs-listener msg))
       ;;"cjbot.crawler.req"   (fn f2 [msg] (println "Pattern match: " (= (first msg) "message")))
       queues-crawler (fn f3 [msg] (println "Pattern match: " msg))}
      (car/subscribe  queue-crawler-req)
      (car/psubscribe queues-crawler))))
  
