(ns cjbot.crawler
  (:gen-class)
  (:use 
   [cjbot.redis]
   [cjbot.mq_utils]
   ;; sources
   [cjbot.sources.twitter])
  (:require 
   [clojure.data.json :as json]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))


;; input and output must be map
(defn cjbot-crawler-reqs-eval [req]
  (let [source (req :source)
        body (req :body)
        key (body :key)
        value (body :value)
        function-string (format "%s-%s" source key)]
    (warn "Calling function " function-string "with param" value)
    (def resp (try (@(resolve(symbol function-string)) value)
                   (catch Exception e (str "caught exception: " (.getMessage e)))))
    (warn "Called function " function-string)
    ;; (debug "  with output=" resp)
    resp))

;; input and output must be json
(defn cjbot-crawler-reqs-listener [msg]
  (println "Pattern match: " msg)
  (case (first msg) 
    "message" (let [req (last msg)
                    req-map (decode-message req)
                    resp (cjbot-crawler-reqs-eval req-map)
                    resp-json (encode-message {:req req :resp resp})]
                (cjbot-mq-publish "crawler.resp" {:req req :resp "ok"}))
    (warn "no action")))
