(ns books.handler
  (:require [books.db                :refer [db coll]]
            [monger.collection       :as mc]
            [pneumatic-tubes.core    :as tubes]
            [pneumatic-tubes.httpkit :as tubes-httpkit])
  (:import org.bson.types.ObjectId))

(defonce cancel-add-requested (atom {}))

(def tx (tubes/transmitter))
(def dispatch-to (partial tubes/dispatch tx))

(defn trace-middleware [handler]
  (fn [tube event-v]
    (println "Received event" event-v "from" tube)
    (handler tube event-v)))

(defn add-book [tube [_ book]]
  (if (= "bad" (:title book))
    (dispatch-to tube [:bad-book])
    (let [book-with-id (assoc book :_id (str (ObjectId.)))]
      (doall (map println (range 15000)))
      (if (get @cancel-add-requested tube)
        (do (println "cancel requested, not saving")
            (swap! cancel-add-requested assoc tube false))
        (do (println "no cancel, saving book")
            (mc/insert (db) coll book-with-id)
            (dispatch-to :all [:acknowledge-book-added book-with-id]))))))

(defn delete-book [tube [_ book-id]]
  (mc/remove-by-id (db) coll book-id)
  ;; (doall (map println (range 20000)))
  (dispatch-to :all [:remove-deleted-book book-id]))

(defn set-name [tube [_ name]]
  (println "Name set to:" name))

(defn initialize-db [tube _]
  (let [book-data (mc/find-maps (db) coll)]
    (println "Initialize-db event received by the server")
    (println book-data)
    (dispatch-to tube [:initialize-db-received book-data])))

(defn cancel-add [tube _]
  (swap! cancel-add-requested assoc tube true)
  (println "Cancel add message received"))

(def handlers
  {:books.events/set-name set-name
   :books.events/set-name-2 set-name
   :books.events/add-book add-book
   :books.events/delete-book delete-book
   :books.events/initialize-db initialize-db
   :books.events/cancel-add cancel-add})

(def wrapped-handlers (tubes/wrap-handlers handlers trace-middleware))

(def rx
  (tubes/receiver wrapped-handlers))

(def handler (tubes-httpkit/websocket-handler rx))
