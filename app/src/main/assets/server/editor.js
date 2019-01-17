;
(function () {
    'use strict';
    var Editor = function Editor(element) {
        this.element = element;
        this.init();
    };
    Editor.prototype.Constants = {
        address: "",
    };
    Editor.prototype.init = function () {
        if (this.element) {
            // https://github.com/sparksuite/simplemde-markdown-editor
            this.mde = new SimpleMDE({
                element: this.element,
                hideIcons: ["guide"],
                spellChecker: false

            });
            this.mde.codemirror.addKeyMap({
                "F1": () => this.onUpdate(),
                "Esc": () => {
                    window.location.hash = "";
                }
            })

            window.onhashchange = this.onHashChange.bind(this);
        }
    };
    Editor.prototype.onUpdate = function () {
        var obj = this.collect();
        if (!obj) return;
        fetch("/api/update", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json; charset=utf-8",
                },
                body: JSON.stringify(obj)
            })
            .then(response => response.text())
            .then(data => {
                console.log(data);
            });
    }
    Editor.prototype.collect = function () {
        var obj = {};
        var hash = window.location.hash;
        if (hash.length < 1) return;
        hash = hash.substring(1);
        obj['id'] = parseInt(hash) || 0;
        var value = this.mde.value().trim();
        if (value.length < 1) return null;;
        var p = value.indexOf('\n');
        var title = "";
        if (p != -1) {
            title = value.substring(0, p).trim();
        } else {
            title = value;
        }
        obj['title'] = title;
        obj['content'] = value;
        return obj;
    }
    Editor.prototype.onFetch = function (hash) {
        fetch("/api/get/" + hash, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json; charset=utf-8",
                }
            })
            .then(response => response.json())
            .then(data => {

                let value = data['content'];
                console.log(value);
                this.mde.value(value);
            });
    }
    Editor.prototype.onHashChange = function (e) {
        var hash = window.location.hash;
        this.onFetch(hash.substring(1));
    };
    window["Editor"] = Editor;

    new Editor(document.getElementById("edit-text"));
})();