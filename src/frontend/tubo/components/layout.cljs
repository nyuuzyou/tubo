(ns tubo.components.layout
  (:require
   [re-frame.core :as rf]
   [tubo.components.loading :as loading]))

(defn logo []
  [:img.mb-1
   {:src   "/images/tubo.png"
    :style {:maxHeight "25px" :maxWidth "40px"}
    :title "Tubo"}])

(defn focus-overlay [on-click-cb active?]
  [:div.w-full.fixed.min-h-screen.right-0.top-0.transition-all.delay-75.ease-in-out
   {:class    "bg-black/50"
    :style    {:visibility (when-not active? "hidden")
               :opacity    (if active? "1" "0")}
    :on-click on-click-cb}])

(defn content-container
  [& children]
  (let [page-loading? @(rf/subscribe [:show-page-loading])
        service-color @(rf/subscribe [:service-color])]
    [:div.flex.flex-col.flex-auto.items-center.px-5.py-4
     (if page-loading?
       [loading/loading-icon service-color "text-5xl"]
       [:div.flex.flex-col.flex-auto.w-full {:class "lg:w-4/5 xl:w-3/5"}
        (map-indexed #(with-meta %2 {:key %1}) children)])]))

(defn uploader-avatar
  [source name & url]
  (let [image [:img.rounded-full.object-cover.max-w-full.min-h-full {:src source :alt name}]]
    (when source
      [:div.relative.w-16.h-16.flex-auto.flex.items-center
       (if url
         [:a.flex-auto.flex {:href url :title name} image]
         image)])))

(defn primary-button
  [label on-click-cb left-icon right-icon]
  [:button.dark:bg-white.bg-stone-800.px-4.rounded-3xl.py-1.outline-none.focus:ring-transparent.whitespace-nowrap
   {:on-click on-click-cb}
   (when left-icon
     [:i.text-neutral-300.dark:text-neutral-800.text-sm
      {:class left-icon}])
   [:span.mx-2.text-neutral-300.dark:text-neutral-900.font-bold.text-sm label]
   (when right-icon
     [:i.text-neutral-300.dark:text-neutral-800.text-sm
      {:class right-icon}])])

(defn secondary-button
  [label on-click-cb left-icon right-icon]
  [:button.dark:bg-transparent.bg-neutral-100.px-4.rounded-3xl.py-1.border.border-neutral-700.dark:border-stone-700.outline-none.focus:ring-transparent.whitespace-nowrap
   {:on-click on-click-cb}
   (when left-icon
     [:i.text-neutral-500.dark:text-white.text-sm
      {:class left-icon}])
   [:span.mx-2.text-neutral-500.dark:text-white.font-bold.text-sm label]
   (when right-icon
     [:i.text-neutral-500.dark:text-white.text-sm
      {:class right-icon}])])
