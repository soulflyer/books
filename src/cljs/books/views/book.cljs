(ns books.views.book
  (:require [re-frame.core :as re-frame]
            [books.subs :as subs]
            [books.events :as events]
            [clojure.string :as str]
            [reagent.core  :as reagent]))

(defn input-element
  ;;TODO If there was a single atom for a book then there would be less duplication and adding fields would be easier
  "An input element which updates its value on change"
  [name placeholder  value]
  [:input {:name name
           :placeholder placeholder
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn title-input [title-atom]
  (input-element "title" "Book title" title-atom))

(defn author-input [author-atom]
  (input-element "author" "Author" author-atom))

(defn keywords-input [keywords-atom]
  (input-element "keywords" "Keywords" keywords-atom))

;;TODO these fields should not just be strings, implement year and http link checking.
(defn published-input [published-atom]
  (input-element "published" "Published date" published-atom))

(defn archive-input [archive-atom]
  (input-element "archive" "Archive link" archive-atom))

(defn book-form []
  (let [title-input-atom     (reagent/atom "")
        author-input-atom    (reagent/atom "")
        published-input-atom (reagent/atom "")
        archive-input-atom   (reagent/atom "")
        keywords-input-atom  (reagent/atom "")]
    (fn []
      [:section#add-book
       [:form#add-book-form
        {:on-submit (fn [e] (.preventDefault e)
                      (re-frame/dispatch [::events/add-book {:title     @title-input-atom
                                                             :author    @author-input-atom
                                                             :published @published-input-atom
                                                             :archive   @archive-input-atom
                                                             :keywords  @keywords-input-atom}]))}
        [title-input     title-input-atom]
        [author-input    author-input-atom]
        [published-input published-input-atom]
        [archive-input   archive-input-atom]
        [keywords-input  keywords-input-atom]
        [:button {:type :submit} "Submit"]]])))

(defn book-item []
  (fn [{:keys [_id title author published archive keywords]}]
    (let [delete-button-visible (re-frame/subscribe [::subs/show-add-book])
          deleting?             (re-frame/subscribe [::subs/deleting])]
      [:div.book
       [:h2
        title ]
       [:div#book-details
        [:h3 author]
        [:p published]
        [:p keywords]
        [:a {:href archive} "Archive-link"]
        (if @delete-button-visible
          (if @deleting?
            [:button {:on-click #(re-frame/dispatch [::events/cancel-delete])} "Cancel"]
            [:button {:on-click #(re-frame/dispatch [::events/delete-book _id])} "Delete"]))]])))

(defn book-list []
  (let [visible-books (re-frame/subscribe [::subs/books])]
    [:section#book-list
     (for [book  @visible-books]
       ^{:key (:_id book)} [book-item book])]))
