(ns books.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
  ::books
  (fn [db]
    (:books db)))

(re-frame/reg-sub
  ::show-add-book
  (fn [db]
    (:show-add-book db)))

(re-frame/reg-sub
  ::deleting
  (fn [db]
    (:deleting db)))
