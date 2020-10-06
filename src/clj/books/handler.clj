(ns books.handler
  (:require [books.db                :refer [db coll]]
            [monger.collection       :as mc]
            [pneumatic-tubes.core    :as tubes]
            [pneumatic-tubes.httpkit :as tubes-httpkit])
  (:import org.bson.types.ObjectId))

(def tx (tubes/transmitter))
(def dispatch-to (partial tubes/dispatch tx))

(defn trace-middleware [handler]
  (fn [tube event-v]
    (println "Received event" event-v "from" tube)
    (handler tube event-v)))

(defn add-book [tube [_ book]]
  ;;(println "Book: " book-with-id " added.")
  ;;(mc/insert (db) coll book-with-id)
  (if (= "abc" (:title book))
    (dispatch-to tube [:bad-book])
    (let [book-with-id (assoc book :_id (str (ObjectId.)))]
      (doall (map println (range 20000)))
      ;; TODO Make this check if a cancel has come in on the same tube before inserting
      (mc/insert (db) coll book-with-id)
      (dispatch-to :all [:acknowledge-book-added book-with-id]))))

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
  ;; TODO This may need to set an atom to stop the add. would need to be keyed to the tube I think.
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
