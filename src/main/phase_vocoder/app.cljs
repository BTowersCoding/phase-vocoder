(ns phase-vocoder.app
  (:require 
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [goog.object :as o]))

(defn square-root
  [x]
  (.sqrt js/Math x))

(defonce audio-context (js/AudioContext.))

(defonce current-sample (r/atom nil))

(defn file-upload []
  [:input#input
   {:type      "file"
    :on-change (fn [e]
                 (let [dom    (o/get e "target")
                       file   (o/getValueByKeys dom #js ["files" 0])
                       reader (js/FileReader.)]
                   (.readAsArrayBuffer reader file)
                   (set! (.-onload reader)
                         (fn [buff] (->
                                 (.decodeAudioData audio-context (-> buff .-target .-result))
                                 (.then #(reset! current-sample %)))))))}])

(defn buffer-source [context buffer]
  (let [source (.createBufferSource context)
        gain (.createGain context)]
    (.resume context)
    (set! (.-buffer source) buffer)
    (.setValueAtTime (.-gain gain) 0.1 (.-currentTime context))
    (.connect source gain)
    (.connect gain (.-destination context))
    (.start source (.-currentTime context))
    source))

(defn app []
  [:div#app
   [:h1 "phase-vocoder"]
   [file-upload]

   [:button
    {:on-click (fn [] (buffer-source audio-context @current-sample))}
    "Play"]])

(defn render []
  (rdom/render [app]
            (.getElementById js/document "root")))

(defn ^:dev/after-load start []
  (render)
  (js/console.log "start"))

(defn ^:export init []
  (js/console.log "init")
  (start))

(defn ^:dev/before-load stop []
  (js/console.log "stop"))
