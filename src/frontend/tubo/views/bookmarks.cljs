(ns tubo.views.bookmarks
  (:require
   [re-frame.core :as rf]
   [tubo.components.items :as items]
   [tubo.components.layout :as layout]
   [tubo.events :as events]))

(defn bookmarks-page
  []
  (let [service-color @(rf/subscribe [:service-color])
        bookmarks @(rf/subscribe [:bookmarks])]
    [layout/content-container
     [:div.flex.justify-between.mt-6
      [:h1.text-3xl.font-nunito-semibold "Bookmarks"]
      [layout/primary-button "Enqueue"
       #(rf/dispatch [::events/enqueue-related-streams bookmarks service-color]) "fa-solid fa-headphones"]]
     [items/related-streams bookmarks]]))
