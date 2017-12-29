(ns tiltontec.tag.gen
  (:refer-clojure :exclude [map meta time])
  (:require
    #?(:cljs [goog.dom.forms :as form])
    #?(:clj [clojure.pprint :refer :all]
       :cljs cljs.pprint :clj :refer [pprint cl-format])
            [tiltontec.cell.base :refer [md-ref? ia-type unbound]]
            [tiltontec.cell.evaluate :refer [not-to-be not-to-be-self]]
            [tiltontec.model.core :refer [make md-get] :as md]))

(def +tag-sid+ (atom -1))

(defn tag-init! []
  (reset! +tag-sid+ -1))

(def tag-by-id (atom {}))

(defn dom-tag [dom]
  (let [tag (get @tag-by-id (.-id dom))]
    (assert tag (str "dom-tag did not find js for id " (.-id dom)
                     " of dom " dom))
    tag))

(defn make-tag [tag attrs aux c?kids]
  (prn :make-tag tag attrs aux)
  (let [tag-id (str (or (:id attrs)
                        (swap! +tag-sid+ inc)))
        mx-tag (apply make
                      :type :tiltontec.tag.html/tag
                      :tag tag
                      :id tag-id
                      :attr-keys (remove #{:id} (keys attrs))
                      :kids c?kids
                      (concat (vec (apply concat (seq (dissoc attrs :id))))
                              (vec (apply concat (seq aux)))))]
    (swap! tag-by-id assoc tag-id mx-tag)
    mx-tag))


(defmethod not-to-be [:tiltontec.tag.html/tag] [me]
  ;; todo: worry about leaks
  (doseq [k (:kids @me)]
    (when (md-ref? k)
      (not-to-be k)))
  (swap! tag-by-id dissoc (md-get me :id))
  (not-to-be-self me))

(defmacro deftag [tag]
  (let [kids (gensym "kids")
        vargs (gensym "vargs")
        tag-name (gensym "tag-name")]
    `(defmacro ~tag [& ~vargs]
       (let [~tag-name (str '~tag)]
         (cond
           (nil? ~vargs)
           `(tiltontec.tag.gen/make-tag ~~tag-name {} {} nil)

           (map? (first ~vargs))
           (cond
             (map? (second ~vargs))
             `(tiltontec.tag.gen/make-tag ~~tag-name ~(first ~vargs) ~(second ~vargs)
                                          ~(when-let [~kids (seq (nthrest ~vargs 2))]
                                             `(tiltontec.model.core/c?kids ~@~kids)))

             :default `(tiltontec.tag.gen/make-tag
                         ~~tag-name ~(first ~vargs)
                         {}
                         ~(when-let [~kids (seq (nthrest ~vargs 1))]
                            `(tiltontec.model.core/c?kids ~@~kids))))

           :default `(tiltontec.tag.gen/make-tag
                       ~~tag-name {} {}
                       (tiltontec.model.core/c?kids ~@~vargs)))))))



(defmacro deftags [& tags]
  `(do ~@(for [tag tags]
           `(deftag ~tag))))

;;; This....
(declare a abbr acronym address applet area article aside audio b base basefont bdi bdo bgsound big blink
         blockquote body br button canvas caption center cite code col colgroup command content
         data datalist dd del details dfn dialog dir div dl dt element em embed
         fieldset figcaption figure font footer form frame frameset
         h1 h2 h3 h4 h5 h6 head header hgroup hr html i iframe image img input ins isindex kbd keygen
         label legend li link listing main map mark marquee menu menuitem meta meter multicol
         nav nextid nobr noembed noframes noscript object ol optgroup option output
         p param picture plaintext pre progress q rp rt rtc ruby
         s samp script section select shadow slot small source spacer span strike strong style sub summary sup
         table tbody td template textarea tfoot th thead time title tr track tt u ul var video wbr xmp)

;;; ...avoids mistaken/benign warnings from this:
(deftags a abbr acronym address applet area article aside audio b base basefont bdi bdo bgsound big blink
         blockquote body br button canvas caption center cite code col colgroup command content
         data datalist dd del details dfn dialog dir div dl dt element em embed
         fieldset figcaption figure font footer form frame frameset
         h1 h2 h3 h4 h5 h6 head header hgroup hr html i iframe image img input ins isindex kbd keygen
         label legend li link listing main map mark marquee menu menuitem meta meter multicol
         nav nextid nobr noembed noframes noscript object ol optgroup option output
         p param picture plaintext pre progress q rp rt rtc ruby
         s samp script section select shadow slot small source spacer span strike strong style sub summary sup
         table tbody td template textarea tfoot th thead time title tr track tt u ul var video wbr xmp)


;;; n.b. Above list of tags needs to be extended, or just use make-tag

;;; --- event conveniences -------------------

(defn evt-tag [e]
  (dom-tag (.-target e)))

#?(:cljs
   (defn target-value [evt]
     (form/getValue (.-target evt))))

;(clojure.pprint/cl-format nil "(deftags ~{~a~^ ~})"
; (for [tag alltags]
;  (clojure.string/replace tag #"[<>]" "")))

;(def alltags '(<a>
;                <abbr>
;                <acronym>
;                <address>
;                <applet>
;                <area>
;                <article>
;                <aside>
;                <audio>
;                <b>
;                <base>
;                <basefont>
;                <bdi>
;                <bdo>
;                <bgsound>
;                <big>
;                <blink>
;                <blockquote>
;                <body>
;                <br>
;                <button>
;                <canvas>
;                <caption>
;                <center>
;                <cite>
;                <code>
;                <col>
;                <colgroup>
;                <command>
;                <content>
;                <data>
;                <datalist>
;                <dd>
;                <del>
;                <details>
;                <dfn>
;                <dialog>
;                <dir>
;                <div>
;                <dl>
;                <dt>
;                <element>
;                <em>
;                <embed>
;                <fieldset>
;                <figcaption>
;                <figure>
;                <font>
;                <footer>
;                <form>
;                <frame>
;                <frameset>
;                <h1>
;                <head>
;                <header>
;                <hgroup>
;                <hr>
;                <html>
;                <i>
;                <iframe>
;                <image>
;                <img>
;                <input>
;                <ins>
;                <isindex>
;                <kbd>
;                <keygen>
;                <label>
;                <legend>
;                <li>
;                <link>
;                <listing>
;                <main>
;                <map>
;                <mark>
;                <marquee>
;                <menu>
;                <menuitem>
;                <meta>
;                <meter>
;                <multicol>
;                <nav>
;                <nextid>
;                <nobr>
;                <noembed>
;                <noframes>
;                <noscript>
;                <object>
;                <ol>
;                <optgroup>
;                <option>
;                <output>
;                <p>
;                <param>
;                <picture>
;                <plaintext>
;                <pre>
;                <progress>
;                <q>
;                <rp>
;                <rt>
;                <rtc>
;                <ruby>
;                <s>
;                <samp>
;                <script>
;                <section>
;                <select>
;                <shadow>
;                <slot>
;                <small>
;                <source>
;                <spacer>
;                <span>
;                <strike>
;                <strong>
;                <style>
;                <sub>
;                <summary>
;                <sup>
;                <table>
;                <tbody>
;                <td>
;                <template>
;                <textarea>
;                <tfoot>
;                <th>
;                <thead>
;                <time>
;                <title>
;                <tr>
;                <track>
;                <tt>
;                <u>
;                <ul>
;                <var>
;                <video>
;                <wbr>
;                <xmp>))