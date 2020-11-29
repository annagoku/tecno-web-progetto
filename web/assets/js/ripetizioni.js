var SERVERURL = window.location.href;
var SESSION_DURATION = 30; //minutes

// per fare in modo che tutte le richieste verso il server dichiarino di volere dei JSON
// sul controller posso quindi distingere le chiamate AJAX da quelle delle risorse HTML
$.ajaxSetup({
    headers:{
        "Accept": "application/json"
    }
});


var app =  new Vue({
    el: "#app",
    data : {
        user: null,
        sessionId: null,
        courses: [],
        teachers: [],
        login : {
            username : "",
            password : "",
            info:null
        },
        modalCatalog: {
            catalog: [],
            errorMessage : null
        }
    },
    mounted : function () {
        this.sessionId = this.getSessionIdLocal();
        this.setScrollingLogic();
        this.getSessionInfo();


    },
    methods: {
        setScrollingLogic : function () {
            var scroll_start = 0;
            var startchange = $('#start-change');
            var offset = startchange.offset();
            var teachers = $('#teachers');
            var offsetT = teachers.offset();

            $(window).resize(function(){
                startchange = $('#start-change');
                offset = startchange.offset();
                teachers = $('#teachers');
                offsetT = teachers.offset();
            });

            $(window).scroll(function(){
                scroll_start = $(document).scrollTop();
                if(scroll_start > offset.top-130 && scroll_start < offsetT.top-50) {
                    $("#navbar").addClass("navbar-opaque");
                    $("#navlink-home").removeClass("active");
                    $("#navlink-teachers").removeClass("active");
                    $("#navlink-courses").addClass("active");

                }
                else if(scroll_start >= offsetT.top-50) {
                    $("#navlink-home").removeClass("active");
                    $("#navlink-teachers").addClass("active");
                    $("#navlink-courses").removeClass("active");
                }
                else{
                    $("#navbar").removeClass("navbar-opaque");
                    $("#navlink-home").addClass("active");
                    $("#navlink-teachers").removeClass("active");
                    $("#navlink-courses").removeClass("active");
                }
            })
        },
        getSessionIdLocal : function() {
            var sessionIDLocal = localStorage.getItem("jsessionid");
            var sessionLastUpdate = localStorage.getItem("jsessionid_lastupdate");
            if(sessionIDLocal == null) {
                localStorage.removeItem("jsessionid_lastupdate");
                return null;
            }
            if(sessionLastUpdate == null) {
                // non dovrebbe succedere ma metto lo stesso come controllo
                localStorage.removeItem("jsessionid");
                return null;
            }
            var now = new Date();
            if(new Date(sessionLastUpdate + SESSION_DURATION*60000)<now) {
                // sessione scaduta
                localStorage.removeItem("jsessionid_lastupdate");
                localStorage.removeItem("jsessionid");
                return null;
            }
            return sessionIDLocal;
        },
        updateSessionId : function (newValue) {
            if(this.getSessionIdLocal() != null) {
                if(newValue) {
                    this.sessionId = newValue;
                    localStorage.setItem("jsessionid", newValue);
                }
                localStorage.setItem("jsessionid_lastupdate", new Date());
            }

        },
        invalidateSession: function () {
            localStorage.removeItem("jsessionid_lastupdate");
            localStorage.removeItem("jsessionid");
            this.sessionId = null;
        },
        getSessionInfo: function () {
            var self = this;

            $.get(this.encodeURL(SERVERURL + 'private/userlog', ""), function (data) {
                //se ok
                self.user = data.user;
                console.log("GetSessionInfo -> " + JSON.stringify(data));
                console.log("sessionID -> "+data.sessionId);
                self.updateSessionId(data.sessionId);
                self.getCourses();
                self.getTeachers();

            }).fail(function () {
                //se errore
                console.log("No sessione utente");
                self.invalidateSession();
            });
        },
        getCourses: function () {
            var self = this;

            $.get(this.encodeURL(SERVERURL+"public/courses","?filter=home"), function (data) {
                //se ok
                self.courses = data;
                console.log("GetCourses -> " + JSON.stringify(data));
                self.updateSessionId();
            }).fail(function (xhr) {
                //se errore
                alert("Errore caricamento corsi -> status " + xhr.status);
                self.updateSessionId();
            });
        },
        encodeURL: function (url, queryString) {
            if(this.sessionId != null) {
                return url+";jsessionid="+this.sessionId+(queryString != null ? queryString : "");
            }
            else {
                return url +(queryString != null ? queryString : "");
            }
        },
        getTeachers: function () {
            var self = this;
            $.get(this.encodeURL(SERVERURL+'public/teachers','?filter=home'), function (data) {
                //se ok
                self.updateSessionId();
                self.teachers = data;
                console.log("GetTeachers -> " + JSON.stringify(data));
            }).fail(function (xhr) {
                //se errore
                self.updateSessionId();
                alert("Si è verificato un errore ->" + xhr.status);
            });
        },
        loginaction: function (e) {
            e.preventDefault();
            var self=this;
            this.login.info = null;
            $.post(this.encodeURL(SERVERURL+'public/login',''), {user:this.login.username, psw:this.login.password}, function (data) {
                console.log("CheckLogin -> " + JSON.stringify(data));
                //se ok
                self.updateSessionId();
                if(!data.result)
                    self.login.info=data;
                else {
                    document.location.href= SERVERURL+"private/";

                }

            }).fail(function (xhr) {
                self.updateSessionId();
                console.log("CheckLogin error code "+xhr.status);
                console.log("CheckLogin error text "+xhr.responseText);
                self.login.info=JSON.parse(xhr.responseText);
                console.log(self.login.info);
            });
        },
        goToPrivateArea : function () {
            document.location.href= SERVERURL+"private/";
        },
        logoutAction: function () {
            var self = this;
            $.get(SERVERURL + 'private/logout', function (data) {
                //se ok
                console.log("Logout -> " + JSON.stringify(data));
                self.user = null;
                self.invalidateSession();
            }).fail(function (xhr) {
                self.updateSessionId();
                alert("Errore sul logout -> status " + xhr.status);
            });
        },
        showCatalog: function () {
            this.modalCatalog.errorMessage = null;
            this.modalCatalog.catalog = [];
            var self = this;
            $.get(this.encodeURL(SERVERURL + 'private/catalog',''), function (data) {
                //se ok
                self.modalCatalog.catalog = data;
                self.updateSessionId();
                console.log("Lessons catalog -> " + JSON.stringify(data));
                $("#modalCatalog").modal('show');
            }).fail(function () {
                //se errore
                self.updateSessionId();
                self.modalCatalog.errorMessage = "Impossibile reperire i dati, riprovare più tardi";
                $("#modalCatalog").modal('show');
            });
        }
    }
});


