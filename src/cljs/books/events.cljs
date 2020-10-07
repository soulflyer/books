(ns books.events
  (:require
   [re-frame.core :as re-frame]
   [books.db :as db]
   [books.subs :as subs]
   [books.tubes :as tubes]))

(defn allocate-temporary-id
  "Returns one more than the current largest id."
  [books]
  (str "temp-id-" (inc (count books))))

;; These are internal re-frame events. Events coming from the server are in books.tubes

(re-frame/reg-event-db
  ::initialize-db
  tubes/send-to-server
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-fx
  ::set-name-2
  (fn [{:keys [db]} [event name]]
    {:db (assoc db :name name)
     :send-to-server [event name]}))

(re-frame/reg-event-db
  ::set-name
  tubes/send-to-server
  (fn [db [_ name]]
    (assoc db :name name)))

(re-frame/reg-event-db
  ::add-book
  tubes/send-to-server
  (fn [db [_ book]]
    (let [books (:books db)
          id (allocate-temporary-id books)
          _book (assoc book :_id id)]
      (-> db
        (assoc :books (conj books _book))
        (assoc :adding true)))))

(re-frame/reg-event-db
  ::delete-book
  tubes/send-to-server
  (fn [db [_ book-id]]
    (assoc db :deleting true)))

(re-frame/reg-event-db
  ::cancel-delete
  (fn [db _]
    ;; TODO Cleanup here. Delete the eagerly added book from the local re-frame db.
    (assoc db :deleting false)))

(re-frame/reg-event-fx
  ::cancel-add
  (fn [{:keys [db]} e]
    (let [server-side-books (re-frame/subscribe [::subs/server-side-books])]
      {:send-to-server e
       :db (-> db
             (assoc :adding false)
             (assoc :books @server-side-books))})))

(re-frame/reg-event-db
  ::show-add-book
  (fn [db [_ _]]
    (assoc db :show-add-book true)))

(re-frame/reg-event-db
  ::hide-add-book
  (fn [db [_ _]]
    (assoc db :show-add-book false)))

(re-frame/reg-event-db
  ::toggle-add-book
  (fn [db [_ _]]
    (assoc db :show-add-book (not (:show-add-book db)))))
