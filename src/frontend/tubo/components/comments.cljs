(ns tubo.components.comments
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [tubo.components.layout :as layout]
   [tubo.events :as events]
   [tubo.utils :as utils]))

(defn comment-item
  [{:keys [id text uploader-name uploader-avatar uploader-url stream-position
           upload-date uploader-verified? like-count hearted-by-uploader?
           pinned? replies reply-count key show-replies author-name author-avatar]}]
  [:div.flex.my-4 {:key key}
   (when uploader-avatar
     [:div.flex.items-center.py-3.box-border.h-12
      (when uploader-url
        [:div.w-12
         [:a {:href (rfe/href :tubo.routes/channel nil {:url uploader-url}) :title uploader-name}
          [:img.rounded-full.object-cover.min-w-full.min-h-full {:src uploader-avatar}]]])])
   [:div.ml-4
    [:div.flex.items-center
     (when pinned?
       [:i.fa-solid.fa-thumbtack.mr-2.text-xs])
     (when uploader-name
       [:div.flex.items-stretch
        [:a {:href (rfe/href :tubo.routes/channel nil {:url uploader-url}) :title uploader-name}
         [:h1.text-neutral-800.dark:text-gray-300.font-bold.line-clamp-1 uploader-name]]
        (when stream-position
          [:div.text-neutral-600.dark:text-neutral-300
           [:span.mx-2.text-xs.whitespace-nowrap (utils/format-duration stream-position)]])])
     (when uploader-verified?
       [:i.fa-solid.fa-circle-check.ml-2])]
    [:div.my-2
     [:p {:dangerouslySetInnerHTML {:__html text} :class "[overflow-wrap:anywhere]"}]]
    [:div..flex.items-center.my-2
     [:div.mr-4
      [:p (utils/format-date-ago upload-date)]]
     (when (and like-count (> like-count 0))
       [:div.flex.items-center.my-2
        [:i.fa-solid.fa-thumbs-up.text-xs]
        [:p.mx-1 like-count]])
     (when hearted-by-uploader?
       [:div.relative.w-4.h-4.mx-2
        [:i.fa-solid.fa-heart.absolute.-bottom-1.-right-1.text-xs.text-red-500]
        [:img.rounded-full.object-covermax-w-full.min-h-full
         {:src author-avatar :title (str author-name " hearted this comment")}]])]
    [:div.flex.items-center.cursor-pointer
     {:on-click #(rf/dispatch [::events/toggle-comment-replies id])}
     (when replies
       (if show-replies
         [:<>
          [:p.font-bold "Hide replies"]
          [:i.fa-solid.fa-turn-up.mx-2.text-xs]]
         [:<>
          [:p.font-bold (str reply-count (if (= reply-count 1) " reply" " replies"))]
          [:i.fa-solid.fa-turn-down.mx-2.text-xs]]))]]])

(defn comments
  [{:keys [comments next-page disabled?]} author-name author-avatar url]
  (let [pagination-loading? @(rf/subscribe [:show-pagination-loading])
        service-color @(rf/subscribe [:service-color])]
    [:div.flex.flex-col
     [:div
      (for [[i {:keys [replies show-replies] :as comment}] (map-indexed vector comments)]
        [:div.flex.flex-col {:key i}
         [comment-item (assoc comment :key i :author-name author-name :author-avatar author-avatar)]
         (when (and replies show-replies)
           [:div {:style {:marginLeft "32px"}}
            (for [[i reply] (map-indexed vector (:items replies))]
              [comment-item (assoc reply :key i :author-name author-name :author-avatar author-avatar)])])])]
     (when (:url next-page)
       (if pagination-loading?
         (layout/loading-icon service-color)
         [:div.flex.justify-center
          [layout/secondary-button
           "Show more comments"
           #(rf/dispatch [::events/comments-pagination url (:url next-page)])
           "fa-solid fa-plus"]]))]))
