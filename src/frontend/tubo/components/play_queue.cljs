(ns tubo.components.play-queue
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [tubo.components.items :as items]
   [tubo.components.layout :as layout]
   [tubo.components.player :as player]
   [tubo.events :as events]
   [tubo.util :as util]))

(defn play-queue-item
  [{:keys [uploader-name uploader-url name duration
           stream url service-color thumbnail-url]} media-queue-pos i]
  (let [service-name (case service-color
                       "#cc0000" "YouTube"
                       "#ff7700" "SoundCloud"
                       "#333333" "media.ccc.de"
                       "#F2690D" "PeerTube"
                       "#629aa9" "Bandcamp")]
    [:div.flex.w-full.h-24.rounded.cursor-pointer.px-2.my-1
     {:class    (when (= i media-queue-pos) "bg-[#f0f0f0] dark:bg-stone-800")
      :on-click #(rf/dispatch [::events/change-media-queue-pos i])}
     [:div.w-56
      [items/thumbnail thumbnail-url nil url name duration {:classes "h-24"}]]
     [:div.flex.flex-col.px-4.py-2.w-full
      [:h1.line-clamp-1 name]
      [:div.text-neutral-600.dark:text-neutral-300.text-sm.flex.flex-col.xs:flex-row
       [:span.line-clamp-1 uploader-name]
       [:span.px-2.hidden.xs:inline-block {:dangerouslySetInnerHTML {:__html "&bull;"}}]
       [:span service-name]]]]))

(defn queue
  []
  (let [show-media-queue       @(rf/subscribe [:show-media-queue])
        loading?               @(rf/subscribe [:show-audio-player-loading])
        paused?                @(rf/subscribe [:paused])
        media-queue            @(rf/subscribe [:media-queue])
        media-queue-pos        @(rf/subscribe [:media-queue-pos])
        {:keys [uploader-name uploader-url
                name stream url service-color]
         :as   current-stream} @(rf/subscribe [:media-queue-stream])
        !elapsed-time          @(rf/subscribe [:elapsed-time])
        !player                @(rf/subscribe [:player])
        loop-playback          @(rf/subscribe [:loop-playback])
        player-ready?          (and @!player (> (.-readyState @!player) 0))]
    (when (and show-media-queue media-queue)
      [:div.fixed.flex.flex-col.items-center.px-5.py-2.min-w-full.w-full.z-30
       {:style {:minHeight "calc(100dvh - 56px)" :height "calc(100dvh - 56px)"}
        :class "dark:bg-neutral-900/90 bg-white/90 backdrop-blur"}
       [:div.flex.justify-between.items-center.w-full.shrink-0
        {:class "lg:w-4/5 xl:w-3/5"}
        [:h1.text-2xl.font-bold.py-6 "Play Queue"]
        [:button.mx-2
         [:i.fa-solid.fa-close
          {:on-click #(rf/dispatch [::events/toggle-media-queue])}]]]
       [:div.flex.flex-col.pr-2.w-full.overflow-y-auto.flex-auto
        {:class "lg:w-4/5 xl:w-3/5"}
        (for [[i item] (map-indexed vector media-queue)]
          ^{:key i} [play-queue-item item media-queue-pos i])]
       [:div.flex.flex-col.py-4.w-full.shrink-0
        {:class "lg:w-4/5 xl:w-3/5"}
        [:div.flex.flex-col.w-full.py-2
         [:a.text-md.line-clamp-1
          {:href (rfe/href :tubo.routes/stream nil {:url url})} name]
         [:a.text-sm.pt-2.text-neutral-600.dark:text-neutral-300.line-clamp-1
          {:href (rfe/href :tubo.routes/channel nil {:url uploader-url})} uploader-name]]
        [:div.flex.flex-auto.py-2.w-full.items-center
         [:span.mr-2 (if @!elapsed-time (util/format-duration @!elapsed-time) "00:00")]
         [player/time-slider !player !elapsed-time service-color]
         [:span.ml-2 (if player-ready? (util/format-duration (.-duration @!player)) "00:00")]]
        [:div.flex.justify-center.items-center
         [player/loop-button loop-playback service-color true]
         [player/button
          [:i.fa-solid.fa-backward-step]
          #(when (and media-queue (not= media-queue-pos 0))
             (rf/dispatch [::events/change-media-queue-pos
                           (- media-queue-pos 1)]))
          :disabled? (not (and media-queue (not= media-queue-pos 0)))
          :extra-styles "text-xl"
          :show-on-mobile? true]
         [player/button
          [:i.fa-solid.fa-backward]
          #(set! (.-currentTime @!player) (- @!elapsed-time 5))
          :extra-styles "text-xl"
          :show-on-mobile? true]
         [player/button
          (if (or loading? (not @!player))
            [layout/loading-icon service-color "text-3xl"]
            (if paused?
              [:i.fa-solid.fa-play]
              [:i.fa-solid.fa-pause]))
          #(rf/dispatch [::events/set-player-paused (not paused?)])
          :extra-styles "text-3xl"
          :show-on-mobile? true]
         [player/button
          [:i.fa-solid.fa-forward]
          #(set! (.-currentTime @!player) (+ @!elapsed-time 5))
          :extra-styles "text-xl"
          :show-on-mobile? true]
         [player/button
          [:i.fa-solid.fa-forward-step]
          #(when (and media-queue (< (+ media-queue-pos 1) (count media-queue)))
             (rf/dispatch [::events/change-media-queue-pos
                           (+ media-queue-pos 1)]))
          :disabled? (not (and media-queue (< (+ media-queue-pos 1) (count media-queue))))
          :extra-styles "text-xl"
          :show-on-mobile? true]]]])))
