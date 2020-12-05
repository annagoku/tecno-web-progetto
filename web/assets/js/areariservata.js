//URL della pagina corrente
var SERVERURL = window.location.href.substr(0,window.location.href.indexOf(";jsessionid"));
var HOMEURL = SERVERURL.substr(0, SERVERURL.indexOf("private"));
var SESSION_DURATION = 30; //minutes

var REGEXP_COURSE_CODE = new RegExp("^[A-Z0-9]{3}$");
var REGEXP_COURSE_NAME = new RegExp("^[A-Z].*$");

// per fare in modo che tutte le richieste verso il server dichiarino di volere dei JSON
// sul controller posso quindi distingere le chiamate AJAX da quelle delle risorse HTML
$.ajaxSetup({
    headers:{
        "Accept": "application/json"
    }
});

var areaRiservataApp= new Vue ({
    el:"#app",
    data: {
        loading: false,
        sessionId: null,
        user: null,
        days: [
            'Lunedì',
            'Martedì',
            'Mercoledì',
            'Giovedì',
            'Venerdì'
        ],
        hours: [{id: 1, startHour: "15:00"},{id: 2, startHour: "16:00"},{id: 3, startHour: "17:00"},{id: 4, startHour: "18:00"}],
        lessonsMatrix : null,
        modalCatalog: {
            catalog: [],
            errorMessage : null
        },
        modalLesson: {
            selectedLesson : null,
            wantConfirm: false,
            wantCancel: false,
            errorMessage: null
        },
        modalNewReservation : {
            users: [],
            courses: [],
            matrix: null,
            userSelected: '-',
            courseSelected: '-',
            errorMessage: null,
            errorMessageCourse: null,
            errorMessageUser: null,
            reservationSelected: null
        },
        modalCheckReservation: {
            state: null,
            feedbackMessage: null
        },
        //VARIABILI AMMINISTRATORE
        courseAdmin: [],
        teacherAdmin: [],
        userAdmin:[],
        associationsAdmin: [],
        lessonsAdmin: [],
        tabActive: "courses",

        modalInsertCourse: {
            code: null,
            name: null,
            image: null,
            errorMessageCode : null,
            errorMessageName: null,
            errorMessageServer: null,
            response: null
        },
        modalInsertTeacher: {
            badge: null,
            name: null,
            surname: null,
            avatar: 'assets/img/soldier.png',
            errorMessageBadge : null,
            errorMessageName: null,
            errorMessageSurname: null,
            errorMessageServer: null,
            response: null
        },
        modalInsertAssociation: {
            teacher: [],
            courseToMatch: [],
            teacherSelected: null,
            courseSelected: '-',
            errorMessageSelectionTeacher: null,
            errorMessageSelectionCourse: null,
            errorMessageServer: null
        },
        alert: {
            message: "",
            action: "none" // "none" "logout"
        },
        modalDeleteAssociation: {
            associationSelected: null,
            warningMessage: null,
            errorMessage:null
        },
        modalDeleteTeacher: {
            teacherSelected: null,
            warningMessage: null,
            errorMessage: null
        },
        modalDeleteCourse: {
            courseSelected: null,
            warningMessage: null,
            errorMessage: null
        },
        modalDeleteReservation: {
            reservationSelected: null,
            warningMessage: null,
            errorMessage: null
        }
    },
    mounted: function () {
        this.sessionId = this.getSessionId();
        this.getSessionInfo();
    },
    methods: {
        getSessionId: function () {
            if(window.location.href.indexOf("=") > 0)
                return window.location.href.substr(window.location.href.indexOf("=")+1, window.location.href.length);
            return null;
        },
//AREA RISERVATA STUDENTE
//recupera da back end le lezioni prenotate dall'utente
        getLessons: function () {
            var self = this;
            $.get(this.encodeURL(SERVERURL + 'lessons',''), function (data) {
                //se ok
                self.lessonsMatrix = data;

                console.log("Lessons Matrix -> " + JSON.stringify(data));
                // nextTick serve per eseguire una funzione solo dopo che il DOM è aggiornato
                // in questo modo posso chiamare la funzione per inizializzare i tooltip
                // sulle card disegnate delle lezioni
                Vue.nextTick(function () {
                    $('[data-toggle="tooltip"]').tooltip()
                });
            }).fail(function (xhr) {
                //se errore
                self.checkServerError(xhr, true);
            });
        },

        showAlert: function (message, action) {
            this.alert.message = message;
            if(action) {
                this.alert.action = action
            }
            else {
                this.alert.action = "none";
            }
            $("#alertDialog").modal('show');
        },
        closeAlert : function () {
            console.log("close alert");
            switch (this.alert.action) {
                case "none":
                    break;
                case "logout":
                    this.invalidateSession();
                    window.location = this.encodeURL(HOMEURL);
                    break;
                default:
                    break;
            }
            this.alert.action = "none";
            $("#alertDialog").modal('hide');
        },
        checkServerError : function (xhr, showAlert) {
            this.loading = false;
            var errorMessage = "Si è verificato un errore generico (status "+xhr.status+")";
            console.log("checkServerError -> status -> "+xhr.status);
            if(xhr.status == 440) {
                this.showAlert("La sessione è scaduta. Verrete rediretti sulla pagina di login", "logout");
                return null;
            }
            if(xhr.status >= 400) {
                try {
                    var json = JSON.parse(xhr.responseText);
                    if(json.errorOccurred) {
                        errorMessage = json.errorOccurred;
                    }
                }
                catch (e) {
                    console.log("Impossibile leggere l'errore in formato json");
                    console.log(e);
                }
            }
            if(showAlert)
                this.showAlert(errorMessage);
            return  errorMessage;
        },
        invalidateSession: function () {
            this.sessionId = null;
            this.user = null;
        },


//recupera le informazioni sull'utente che ha fatto login
        getSessionInfo: function () {
            var self = this;
            this.loading = true;
            $.get(this.encodeURL(SERVERURL + 'userlog',''), function (data) {
                //se ok
                self.user = data.user;
                self.loading=false;
                if (self.user == null) {
                    window.location = self.encodeURL(HOMEURL);
                    return;
                }
                if(self.user.role === "student") {
                    self.getLessons();
                }
                else{
                    self.getCourses(null, false);
                }
                console.log("GetSessionInfo -> " + JSON.stringify(data));
            }).fail(function () {
                self.loading=false;
                //se errore
                this.showAlert("Impossibile reperire informazioni utente! Verrete rediretti sulla pagina di login", "logout");

            });
        },

//rappresenta in modo diverso le lezioni prenotate in base al loro stato : prenotato-confermato-annullato
        lessonClass: function (state) {
            switch (state) {
                case 1:
                    return "text-dark bg-warning";
                case 2:
                    return "text-white bg-success";
                case 3:
                    return "text-white bg-secondary";
            }
            return "text-white bg-info"
        },

//rappresenta in modo diverso le lezioni prenotate in base al loro stato associando anche la relativa icona: prenotato-confermato-annullato
        lessonIcon: function (state) {
            switch (state) {
                case 1:
                    return "fas fa-clock";
                case 2:
                    return "fas fa-check-circle";
                case 3:
                    return "fas fa-times-circle";
                default:
                    return "";
            }
        },

//apertura di una modale per cambiare lo stato di una prenotazione
        showModalLesson: function (k) {
            this.modalLesson.selectedLesson = k;
            if (this.modalLesson.selectedLesson.state.code == 1) {
                this.modalLesson.wantConfirm = false;
                this.modalLesson.wantCancel = false;
                this.modalLesson.errorMessage = null;
                $('#modalState').modal();
            }
        },

//Variazione dello stato di una prenotazione effettuata
        setModalLessonState: function (n) {
            if (n == 1) {
                this.modalLesson.wantConfirm = true;
                this.modalLesson.wantCancel = false;
            } else {
                this.modalLesson.wantConfirm = false;
                this.modalLesson.wantCancel = true;
            }
        },

//Salvataggio del nuovo stato di una prenotazione
        saveLessonState: function () {
            var self = this;
            var statecode;
            this.modalLesson.errorMessage = null;
            if (!this.modalLesson.wantConfirm && !this.modalLesson.wantCancel) {
                this.modalLesson.errorMessage = "Seleziona una azione o annulla";
                return;
            }
            if (this.modalLesson.wantConfirm) {
                statecode = 2;
            } else {
                statecode = 3;
            }
            self.loading=true;
            $.post(this.encodeURL(SERVERURL + 'lessons',''), {
                idLesson: this.modalLesson.selectedLesson.id,
                action: "modificastato",
                stateLesson: statecode
            }, function (data) {
                self.loading=false;

                console.log("CheckChangeState -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalLesson.errorMessage = data.errorOccurred;
                } else {
                    $('#modalState').modal('hide');
                    self.getLessons();
                }

            }).fail(function (xhr) {
                self.loading=false;
                self.modalLesson.errorMessage = self.checkServerError(xhr, false);

            });
        },

//Visualizzazione dell'intero catalogo di lezioni disponibili
        showCatalog: function () {
            this.modalCatalog.errorMessage = null;
            this.modalCatalog.catalog = [];
            var self = this;
            self.loading=true;
            $.get(this.encodeURL(SERVERURL + 'catalog',''), function (data) {
                //se ok
                self.loading=false;

                self.modalCatalog.catalog = data;

                console.log("Lessons catalog -> " + JSON.stringify(data));
                $("#modalCatalog").modal('show');
            }).fail(function (xhr) {
                //se errore
                self.checkServerError(xhr, false);
                self.modalCatalog.errorMessage = "Impossibile reperire i dati, riprovare più tardi";
                $("#modalCatalog").modal('show');
            });
        },
//apertura di una modale con selezione della materia di interesse e visualizzazione delle disponibilità
        showModalNewReservation: function () {
            var self = this;
            this.modalNewReservation.userSelected = '-';
            this.modalNewReservation.courseSelected = '-';
            this.modalNewReservation.courses = [];
            this.modalNewReservation.matrix = null;
            this.modalNewReservation.errorMessage = null;
            this.modalNewReservation.errorMessageCourse = null;
            this.modalNewReservation.errorMessageUser = null;
            self.loading=true;
            $.get(this.encodeURL(HOMEURL + "public/courses","?filter=home"), function (data) {
                //se ok
                self.loading=false;

                self.modalNewReservation.courses = data;
                $('#modalNew').modal('show');
                console.log("GetCourses -> " + JSON.stringify(data));
            }).fail(function (xhr) {
                //se errore
                self.modalNewReservation.errorMessage = self.checkServerError(xhr, false);
                $('#modalNew').modal('show');
            });
        },

//selezionata la materia permette di caricarne su timetable le disponibilità
        onSelectCourse: function (e) {
            var codeMat = this.modalNewReservation.courseSelected;
            var idUser = this.modalNewReservation.userSelected;
            var self = this;
            console.log("Selected course code -> " + codeMat);
            this.modalNewReservation.errorMessage = null;

            if(this.user.role == 'administrator') {
                console.log("Selected user id -> " + idUser);
                var result = true;
                if(codeMat === null || codeMat === '-') {
                    this.modalNewReservation.matrix = null;
                    result = false;
                }
                else {
                    this.modalNewReservation.errorMessageCourse = null;
                }
                if((idUser === null || idUser === '-')) {
                    this.modalNewReservation.matrix = null;
                    result = false;
                }
                else {
                    this.modalNewReservation.errorMessageUser = null;
                }
                if(!result) {
                    return;
                }
            }
            else {
                //UTENTE
                if(codeMat === null || codeMat === '-') {
                    console.log("course not selected");
                    this.modalNewReservation.matrix = null;
                    return;
                }
                else {
                    this.modalNewReservation.errorMessageCourse = null;
                }
            }

            self.loading=true;
            $.get(this.encodeURL(SERVERURL + 'courseavailability','?courseCode=' + codeMat), function (data) {
                self.loading=false;
                console.log("Select availability course -> " + JSON.stringify(data));
                self.modalNewReservation.matrix = data;

            }).fail(function (xhr) {
                console.log("Retrieve course availability " + xhr.status);
                self.modalNewReservation.errorMessage = self.checkServerError(xhr, false);

            });

        },
//selezione di una nuova prenotazione tramite click su timetable
        clickReservation: function (catalogItem) {
            //console.log("Item selected " + JSON.stringify(catalogItem))
            if (this.modalNewReservation.reservationSelected != null) {
                this.modalNewReservation.reservationSelected.selected = false;
            }
            catalogItem.selected = true;
            this.modalNewReservation.reservationSelected = catalogItem;

        },
        encodeURL: function (url, queryString) {
            if(this.sessionId != null) {
                return url+";jsessionid="+this.sessionId+(queryString != null ? queryString : "");
            }
            else {
                return url +(queryString != null ? queryString : "");
            }
        },
//controlla e gestisce eventuali sovrapposizioni tra nuova prenotazione selezionata e prenotazioni esistenti
        checkFeasibilityNewReservation : function(){
            this.modalNewReservation.errorMessage = null;
            this.modalNewReservation.errorMessageUser = null;
            this.modalNewReservation.errorMessageCourse = null;
            console.log("checkFeasibility");
            var j;
            var i;

            var result = true;
            // check input
            if(this.user.role == 'administrator') {
                if(this.modalNewReservation.userSelected == '-') {
                    this.modalNewReservation.errorMessageUser = "Selezionare un utente";
                    result = false;
                }
            }
            if(this.modalNewReservation.courseSelected == '-') {
                this.modalNewReservation.errorMessageCourse = "Selezionare un corso";
                result = false;
            }


            if(!result) {
                return ;
            }

            this.modalCheckReservation.feedbackMessage=null;
            if(this.modalNewReservation.reservationSelected) {
                $('#modalNew').modal('hide');
                if(this.user.role == 'student') {


                    i = this.modalNewReservation.reservationSelected.slot.id - 1;
                    j = this.modalNewReservation.reservationSelected.day.daycode - 1;
                    console.log("checkFeasibility i: "+i+", j: "+j);
                    if (this.lessonsMatrix[i][j].length > 0) {
                        console.log(this.lessonsMatrix[i][j]);
                        this.modalCheckReservation.state = 3;
                        for(var k=0; k<this.lessonsMatrix[i][j].length; k++) {
                            if(this.lessonsMatrix[i][j][k].state.code !== 3) {
                                this.modalCheckReservation.state = this.lessonsMatrix[i][j][k].state.code;
                            }
                        }

                        switch (this.modalCheckReservation.state) {
                            case 1:
                                $('#modalCheckFeasibility').modal('show');
                                return this.modalCheckReservation.feedbackMessage = "Hai già una prenotazione nello slot scelto. Vuoi annullare e sovrascrivere?";
                            case 2 :
                                $('#modalCheckFeasibility').modal('show');
                                return this.modalCheckReservation.feedbackMessage = "Hai già una prenotazione effettuata nello slot scelto. Impossibile sostituire una prenotazione effettuata";
                            case 3:
                                console.log("checkFeasibility -> posso effettuare la prenotazione");
                                return this.saveNewReservation();

                        }
                    } else {
                        console.log("checkFeasibility -> posso effettuare la prenotazione");
                        return this.saveNewReservation();
                    }
                }
                else {
                    this.saveNewReservationAdmin();
                }

            }else{
                this.modalNewReservation.errorMessage="Seleziona una lezione da prenotare o annulla";
            }
        },

        switchTab: function(tab) {
            var self = this;
            switch (tab) {
                case 'courses':

                    this.getCourses(function () {
                        self.tabActive="courses";
                        }, false);
                    break;
                case 'teachers':
                    this.getTeachersAdmin(function () {
                        self.tabActive="teachers";
                    });
                    break;
                case 'associations':
                    this.getAssociationsAdmin(function () {
                        self.tabActive="associations";

                    });
                    break;
                case 'lessons':

                    this.getLessonAdmin(function () {
                        self.tabActive="lessons";
                    });
                    break;

            }
        },


//Salvataggio nuova prenotazione su db
        saveNewReservation: function () {
            console.log("saveNewReservation");
            $('#modalCheckFeasibility').modal('hide');
            var self=this;
            this.modalNewReservation.reservationSelected.selected = false;
            self.loading=true;
            $.post(this.encodeURL(SERVERURL + 'newReservation',''), {
                infoCatalogItemSelected: JSON.stringify(this.modalNewReservation.reservationSelected),
            }, function (data) {
                self.loading=false;

                console.log("CheckNewReservation -> " + JSON.stringify(data));
                //se ko

                if (!data.result) {
                    self.showAlert(data.errorMessage);
                } else {
                    $('#modalNew').modal('hide');
                    self.getLessons();
                }
            }).fail(function (xhr) {
                self.checkServerError(xhr, true);
            });
        },

        saveNewReservationAdmin: function () {
            console.log("saveNewReservationAdmin");
            var self=this;
            this.modalNewReservation.reservationSelected.selected = false;
            self.loading=true;
            $.post(this.encodeURL(SERVERURL + 'newReservation',''), {
                infoCatalogItemSelected: JSON.stringify(this.modalNewReservation.reservationSelected),
                userId: this.modalNewReservation.userSelected
            }, function (data) {
                self.loading=false;

                console.log("CheckNewReservation -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.showAlert(data.errorMessage);
                } else {
                    $('#modalNew').modal('hide');
                    self.getLessonAdmin();
                }
            }).fail(function (xhr) {
                self.checkServerError(xhr, true);
            });
        },
        goToHome: function () {
            document.location.href = this.encodeURL(HOMEURL);
        },
//Logout utente e ritorno alla home
        logoutAction: function () {
            var self = this;
            self.loading=true;
            $.get(this.encodeURL(SERVERURL + 'logout',''), function (data) {
                //se ok
                self.loading=false;

                self.invalidateSession();
                console.log("Logout -> " + JSON.stringify(data));
                if (data.result == true) {
                    var home = self.encodeURL(HOMEURL);
                    console.log("logout successful... redirecting to home "+home)
                    document.location.href = home;
                } else {
                    self.showAlert(data.errorMessage);
                }
            }).fail(function (xhr) {
                self.checkServerError(xhr, true);
            });
        },

        //AREA RISERVATA ADMINISTRATOR
        getCourses: function (callback, onlyactive) {
            var path = this.encodeURL(HOMEURL + 'public/courses' , onlyactive ? '?filter=home' : '?filter=admin');
            var self = this;

            self.loading=true;
            $.get(path, function (data) {
                //se ok
                self.loading=false;

                self.courseAdmin=data;
                console.log("courseadmin -> " + JSON.stringify(data));
                if(callback)
                    callback();


            }).fail(function (xhr) {
                self.checkServerError(xhr, true);
            });

        },

        getTeachersAdmin: function (callback) {
            var self=this;
            self.loading=true;
            $.get(this.encodeURL(HOMEURL + 'public/teachers','?filter=admin'), function (data) {
                //se ok
                self.loading=false;

                self.teacherAdmin=data;
                console.log("teacheradmin -> " + JSON.stringify(data));
                if(callback)
                    callback();

            }).fail(function (xhr) {
                self.checkServerError(xhr, true);
            });
        },

        getAssociationsAdmin: function (callback) {
            var self=this;
            self.loading=true;

            $.get(this.encodeURL(SERVERURL + 'associationsadmin',''), function (data) {
                //se ok
                self.loading=false;

                self.associationsAdmin=data;
                console.log("courses & teacheradmin -> " + JSON.stringify(data));
                if(callback)
                    callback();
            }).fail(function (xhr) {
                self.checkServerError(xhr, true);
            });
        },

        getLessonAdmin: function (callback) {
            var self=this;

            self.loading=true;

            $.get(this.encodeURL(SERVERURL + 'lessons',''), function (data) {
                //se ok
                self.loading=false;

                self.lessonsAdmin=data;
                console.log("Lessons -> " + JSON.stringify(data));
                if(callback)
                    callback();
            }).fail(function (xhr) {
                self.checkServerError(xhr, true);
            });
        },
        getUserAdmin: function (callback) {
            var self = this;
            self.loading=true;
            $.get(this.encodeURL(SERVERURL + 'userlist',''), function (data) {
                //se ok
                self.loading=false;

                console.log("user List -> " + JSON.stringify(data));
                self.userAdmin=data;
                if(callback)
                    callback();

            }).fail(function (xhr) {
                self.checkServerError(xhr, true);
            });
        },

        refresh: function(p){
            switch (p) {
                case 1:
                    this.getCourses(null, false);
                    break;
                case 2:
                    this.getTeachersAdmin(null);
                    break;
                case 3:
                    this.getAssociationsAdmin(null);
                    break;
                case 4:
                    this.getLessonAdmin(null);
                    break;
            }
        },

        clickFabAdmin: function () {
            switch (this.tabActive) {
                case "associations":
                    this.modalInsertAssociation.teacherSelected='-';
                    this.modalInsertAssociation.courseSelected='-';
                    this.modalInsertAssociation.errorMessageSelectionTeacher=null;
                    this.modalInsertAssociation.errorMessageSelectionCourse=null;
                    this.modalInsertAssociation.errorMessageServer=null
                    var self = this;
                    this.getTeachersAdmin(
                        function () {
                            self.modalInsertAssociation.teacher = self.teacherAdmin;
                            $('#insertAssociation').modal('show');
                        }
                    );

                    break;
                case "courses":
                    this.modalInsertCourse.code=null;
                    this.modalInsertCourse.name=null;
                    this.modalInsertCourse.image=null;
                    this.modalInsertCourse.errorMessageServer = null;
                    this.modalInsertCourse.errorMessageName = null;
                    this.modalInsertCourse.errorMessageCode = null;
                    $('#insertCourse').modal('show');
                    break;
                case "lessons":
                    var self=this;
                    this.modalNewReservation.courseSelected = '-';
                    this.modalNewReservation.userSelected = '-';
                    this.modalNewReservation.errorMessage = null;
                    this.modalNewReservation.errorMessageCourse = null;
                    this.modalNewReservation.errorMessageUser = null;

                    this.modalNewReservation.matrix = null;
                    this.getUserAdmin(
                        function () {
                            self.modalNewReservation.users = self.userAdmin;

                        }

                    );
                    this.getCourses(
                        function () {
                            self.modalNewReservation.courses=self.courseAdmin;
                            $('#modalNew').modal('show');
                        }, true
                    );

                    break;
                case "teachers":
                    this.modalInsertTeacher.badge=null;
                    this.modalInsertTeacher.name=null;
                    this.modalInsertTeacher.surname=null;
                    this.modalInsertTeacher.avatar='assets/img/soldier.png';
                    this.modalInsertTeacher.errorMessageBadge=null;
                    this.modalInsertTeacher.errorMessageName=null;
                    this.modalInsertTeacher.errorMessageSurname=null;
                    this.modalInsertTeacher.errorMessageServer=null;
                    $('#insertTeacher').modal('show');
                    break;
            }

        },

        checkInputNewCourse: function(e){
            e.preventDefault();
            var result = true;

            this.modalInsertCourse.errorMessageCode=null;
            this.modalInsertCourse.errorMessageName=null;
            if(this.modalInsertCourse.code==null ||  !REGEXP_COURSE_CODE.test(this.modalInsertCourse.code)){
                this.modalInsertCourse.errorMessageCode="Campo obbligatorio di 3 caratteri solo alfanumerici";
                result = false;
            }

            if(this.modalInsertCourse.name==null|| !REGEXP_COURSE_NAME.test(this.modalInsertCourse.name)){
                this.modalInsertCourse.errorMessageName="Campo obbligatorio. Inserire solo caratteri alfanumerici";
                result = false;
            }

            if(result) {
                this.insertNewCourseAdmin();
            }

        },
        insertNewCourseAdmin: function (){
            var self=this;

            this.modalInsertCourse.errorMessageServer=null;
            self.loading=true;
            $.post(this.encodeURL(HOMEURL + 'public/courses',''), {
                newcode: this.modalInsertCourse.code, newname: this.modalInsertCourse.name, newimage: this.modalInsertCourse.image
            }, function (data) {
                self.loading=false;

                console.log("InsertNewCourse -> " + JSON.stringify(data));
                //se ko
                self.response=data;
                if (!self.response.result) {
                    this.modalInsertCourse.errorMessageServer=self.response.errorOccurred;
                } else {
                    $('#insertCourse').modal('hide');

                    self.getCourses(null, false);
                }
            }).fail(function (xhr) {
                self.modalInsertCourse.errorMessageServer = self.checkServerError(xhr, false);

            });

        },

        checkInputNewTeacher: function(e){
            e.preventDefault();
            var expressionAlphabetic = new RegExp('^[A-zÀ-ú ]+$','i');
            var expressionNumber=new RegExp('^[0-9]+$','i');
            var result = true;
            this.modalInsertTeacher.errorMessageBadge=null;
            this.modalInsertTeacher.errorMessageName=null;
            this.modalInsertTeacher.errorMessageSurname=null;
            this.modalInsertTeacher.errorMessageServer=null;


            if(this.modalInsertTeacher.badge==null || this.modalInsertTeacher.badge.length!=6  || !expressionNumber.test(this.modalInsertTeacher.badge)){
                this.modalInsertTeacher.errorMessageBadge="Campo obbligatorio di 6 caratteri numerici";
                result=false;
            }

            if(this.modalInsertTeacher.name==null || !expressionAlphabetic.test(this.modalInsertTeacher.name)){
                this.modalInsertTeacher.errorMessageName="Campo obbligatorio. Inserire solo caratteri alfabetici";
                result=false;
            }
            if(this.modalInsertTeacher.surname==null|| !expressionAlphabetic.test(this.modalInsertTeacher.surname)){
                this.modalInsertTeacher.errorMessageSurname="Campo obbligatorio. Inserire solo caratteri alfabetici";
                result=false;
            }

            if(result){
                this.insertNewTeacherAdmin();
            }
        },
        insertNewTeacherAdmin: function (){
            var self=this;

            this.modalInsertTeacher.errorMessageServer=null;
            self.loading=true;
            $.post(this.encodeURL(HOMEURL + 'public/teachers','?filter=admin'), {
                newbadge: this.modalInsertTeacher.badge,
                newname: this.modalInsertTeacher.name,
                newsurname: this.modalInsertTeacher.surname,
                newavatar: this.modalInsertTeacher.avatar
            }, function (data) {
                self.loading=false;

                console.log("InsertNewTeacher -> " + JSON.stringify(data));
                //se ko
                self.response=data;
                if (!self.response.result) {
                    this.modalInsertTeacher.errorMessageServer=self.response.errorOccurred;
                } else {
                    $('#insertTeacher').modal('hide');
                    self.getTeachersAdmin(null);
                }
            }).fail(function (xhr) {
                self.modalInsertTeacher.errorMessageServer = self.checkServerError(xhr, false);

            });
        },

        onSelectTeacher: function (e) {
            var badge = e.target.value;
            var self = this;
            if(badge != '-') {
                this.modalInsertAssociation.errorMessageSelectionTeacher=null;

                this.modalInsertAssociation.teacherSelected=badge;
                self.loading=true;
                $.get(this.encodeURL(SERVERURL + 'courseList','?badge=' + badge), function (data) {
                    console.log("Select new course for teacher selected -> " + JSON.stringify(data));
                    self.loading=false;

                    self.modalInsertAssociation.courseToMatch = data;

                }).fail(function (xhr) {
                    self.modalInsertAssociation.errorMessage = self.checkServerError(xhr, false);

                });
            }
            console.log("Selected teacher badge -> " + badge);
        },

        onSelectTeacherCourse : function(e) {
            var course = e.target.value;
            if(course != '-') {
                this.modalInsertAssociation.errorMessageSelectionCourse = null;
            }
        },

        saveNewAssociation: function (e) {
            e.preventDefault();
            this.modalInsertAssociation.errorMessageSelectionTeacher=null;
            this.modalInsertAssociation.errorMessageSelectionCourse=null;
            var self=this;
            var result = true;
            if(this.modalInsertAssociation.teacherSelected==null || this.modalInsertAssociation.teacherSelected== '-'){
                this.modalInsertAssociation.errorMessageSelectionTeacher="Selezionare un'insegnate";
                result = false;
            }
            if(this.modalInsertAssociation.courseSelected==null || this.modalInsertAssociation.courseSelected== '-'){
                this.modalInsertAssociation.errorMessageSelectionCourse="Selezionare un corso tra quelli proposti";
                result = false;
            }
            if(result) {

                self.loading=true;
                $.post(this.encodeURL(SERVERURL + 'courselist',''), {
                    badgeNumber: this.modalInsertAssociation.teacherSelected,
                    codCourse: this.modalInsertAssociation.courseSelected
                }, function (data) {
                    self.loading=false;

                    console.log("InsertNewAssociation -> " + JSON.stringify(data));
                    //se ko
                    self.response=data;
                    if (!self.response.result) {
                        self.modalInsertAssociation.errorMessageServer=self.response.errorOccurred;
                    } else {
                        $('#insertAssociation').modal('hide');
                        self.getAssociationsAdmin();
                    }
                }).fail(function (xhr) {
                    self.modalInsertAssociation.errorMessageServer = self.checkServerError(xhr, false);
                });


            }
        },
        changeStateAssociation : function (tc) {
            this.modalDeleteAssociation.associationSelected=tc;
            this.modalDeleteAssociation.warningMessage="Attenzione: la cancellazione della associazione " +
                "selezionata potrebbe comportare la cancellazione di prenotazioni effettuate. Vuoi Procedere?"
            $('#deleteAssociation').modal('show');

        },
        saveDeleteAssociation : function () {
            var self=this;
            self.loading=true;
            $.post(this.encodeURL(SERVERURL + 'deleteassociation',''), {
                associationToDelete: JSON.stringify(this.modalDeleteAssociation.associationSelected)
            }, function (data) {
                self.loading=false;

                console.log("Delete association -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalDeleteAssociation.errorMessage=data.errorOccurred;
                } else {
                    $('#deleteAssociation').modal('hide');
                    self.getAssociationsAdmin();
                }
            }).fail(function (xhr) {
                self.modalDeleteAssociation.errorMessage=self.checkServerError(xhr, false);

            });

        },
        changeStateTeacher : function (t) {
            this.modalDeleteTeacher.teacherSelected=t;
            this.modalDeleteTeacher.warningMessage="Attenzione: la cancellazione del docente " +
                "selezionato potrebbe comportare la cancellazione di prenotazioni effettuate. Vuoi Procedere?"
            $('#deleteTeacher').modal('show');

        },
        saveDeleteTeacher : function () {
            var self=this;
            self.loading=true;
            $.post(this.encodeURL(SERVERURL + 'deleteteacher',''), {
                teacherToDelete: JSON.stringify(this.modalDeleteTeacher.teacherSelected)
            }, function (data) {
                self.loading=false;

                console.log("Delete teacher -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalDeleteTeacher.errorMessage=data.errorOccurred;
                } else {
                    $('#deleteTeacher').modal('hide');
                    self.getTeachersAdmin();
                }
            }).fail(function (xhr) {
                self.modalDeleteTeacher.errorMessage=self.checkServerError(xhr, false);
            });
        },

        changeStateCourse : function (c) {
            this.modalDeleteCourse.courseSelected=c;
            this.modalDeleteCourse.warningMessage="Attenzione: la cancellazione del corso " +
                "selezionato potrebbe comportare la cancellazione di prenotazioni effettuate. Vuoi Procedere?"
            $('#deleteCourse').modal('show');

        },
        saveDeleteCourse : function () {
            var self=this;
            self.loading=true;
            $.post(this.encodeURL(SERVERURL + 'deletecourse',''), {
                courseToDelete: JSON.stringify(this.modalDeleteCourse.courseSelected)
            }, function (data) {
                self.loading=false;

                console.log("Delete course -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalDeleteCourse.errorMessage=data.errorOccurred;
                } else {
                    $('#deleteCourse').modal('hide');
                    self.getCourses(null, false);
                }
            }).fail(function (xhr) {

                self.modalDeleteCourse.errorMessage=self.checkServerError(xhr, false);
            });
        },
        changeStateReservation : function (r) {
            this.modalDeleteReservation.reservationSelected=r;
            this.modalDeleteReservation.warningMessage="Attenzione: Confermi la cancellazione della prenotazione selezionata? Vuoi Procedere?"
            $('#modalDeleteReservationAdmin').modal('show');

        },
        saveDeleteReservation : function () {
            var self=this;
            self.loading=true;
            $.post(this.encodeURL(SERVERURL + 'deletereservation',''), {
                reservationToDelete: JSON.stringify(this.modalDeleteReservation.reservationSelected)
            }, function (data) {
                self.loading=false;

                console.log("Delete reservation -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalDeleteReservation.errorMessage=data.errorOccurred;
                } else {
                    $('#modalDeleteReservationAdmin').modal('hide');
                    self.getLessonAdmin();
                }
            }).fail(function (xhr) {
                self.modalDeleteReservation.errorMessage=self.checkServerError(xhr, false);
            });
        }

    }
});






