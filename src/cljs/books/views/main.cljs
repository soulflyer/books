(ns books.views.main
  (:require [re-frame.core :as re-frame]
            [books.subs :as subs]
            [books.events :as events]
            [books.views.book :refer [book-form book-list]]
            [reagent.core  :as reagent]))

(defn header []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div#header
     [:h1 @name]
     [:button {:on-click #(re-frame/dispatch [::events/toggle-add-book])} "Add book"]]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])
        add-book-visible? (re-frame/subscribe [::subs/show-add-book])
        deleting? (re-frame/subscribe [::subs/deleting])
        adding? (re-frame/subscribe [::subs/adding])]
    [:<>
     [header]
     [:div#main
      (if @add-book-visible? [book-form])
      [book-list]
      (if (or @deleting? @adding?)
        [:div#message
         [:h2 "please wait"]
         [:button {:on-click #(re-frame/dispatch [::events/cancel-add])} "Cancel add"]])]]))
