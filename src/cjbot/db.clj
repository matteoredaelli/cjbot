(ns cjbot.db
  (:gen-class)
  (:use 
   [cjbot.mq_utils]
   [cjbot.redis]
   [cjbot.mongodb]
   )
  (:require 
   [monger.core :as mg]
   [monger.collection :as mc]
   [monger.operators :refer :all]
   [clojure.data.json :as json]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

;; input and output must be map
(defn cjbot-db-update [req]
  (let [object (format "%s_%s" (req :source) (req :object))
        id (req :id)
        attribute (req :attribute)]
    (warn "Saving to DB object =" object "id =" id)
    (mc/update object {:_id id} {$set attribute} :upsert true)
    (warn "..Done")
    "ok"
    ))

;; input and output must be json
(defn cjbot-db-update-reqs-listener [msg]
  (debug "Pattern match: " msg)
  (case (first msg) 
    "message" (let [req (last msg)
                    req-map (decode-message req)
                    resp (cjbot-db-update req-map)]
                (cjbot-mq-publish "db.update.resp" {:req req :resp resp}))
    (warn "no action for message type =" (first msg))))

