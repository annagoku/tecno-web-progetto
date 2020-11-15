//URL della pagina corrente
var SERVERURL = window.location.href;
var HOMEURL = SERVERURL.substr(0, SERVERURL.indexOf("private"));

window.userSession = null;

var areaRiservataApp= new Vue ({
    el:"#app",
    data: {
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
            message: ""
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
        }


    },
    mounted: function () {
        this.getSessionInfo();
    },
    methods: {
//AREA RISERVATA STUDENTE
//recupera da back end le lezioni prenotate dall'utente
        getLessons: function () {
            var self = this;
            $.get(SERVERURL + 'lessons', function (data) {
                //se ok
                self.lessonsMatrix = data;
                console.log("Lessons Matrix -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                alert("Impossibile reperire le informazioni delle lezioni!");

            });
        },

        showAlert: function (message) {
            this.alert.message = message;
            $("#alertDialog").modal('show');
        },

//recupera le informazioni sull'utente che ha fatto login
        getSessionInfo: function () {
            var self = this;
            $.get(SERVERURL + 'userlog', function (data) {
                //se ok
                self.user = data;
                if (self.user == null) {
                    window.location = HOMEURL;
                    return;
                }
                if(self.user.role === "student") {
                    self.getLessons();
                }
                else{
                    self.getCourses();
                }
                console.log("GetSessionInfo -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                alert("Impossibile reperire informazioni utente!");
                window.location = HOMEURL;
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
                this.modalLesson.errorMessage = null;
                this.modalLesson.wantConfirm = false;
                this.modalLesson.wantCancel = false;
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
            $.post(SERVERURL + 'lessons', {
                idLesson: this.modalLesson.selectedLesson.id,
                action: "modificastato",
                stateLesson: statecode
            }, function (data) {
                console.log("CheckChangeState -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalLesson.errorMessage = data.errorOccurred;
                } else {
                    $('#modalState').modal('hide');
                    self.getLessons();
                }

            }).fail(function (xhr) {
                console.log("Save lesson state error code " + xhr.status);
                self.modalLesson.errorMessage = "Si è verificato un errore";

            });
        },

//Visualizzazione dell'intero catalogo di lezioni disponibili
        showCatalog: function () {
            this.modalCatalog.errorMessage = null;
            this.modalCatalog.catalog = [];
            var self = this;
            $.get(SERVERURL + 'catalog', function (data) {
                //se ok
                self.modalCatalog.catalog = data;
                console.log("Lessons catalog -> " + JSON.stringify(data));
                $("#modalCatalog").modal('show');
            }).fail(function () {
                //se errore
                self.modalCatalog.errorMessage = "Impossibile reperire i dati, riprovare più tardi";
                $("#modalCatalog").modal('show');
            });
        },
//apertura di una modale con selezione della materia di interesse e visualizzazione delle disponibilità
        showModalNewReservation: function () {
            var self = this;
            this.modalNewReservation.courseSelected = '-';
            this.modalNewReservation.courses = [];
            this.modalNewReservation.matrix = null;
            this.modalNewReservation.errorMessage = null;
            $.get(HOMEURL + "public/courses?filter=home", function (data) {
                //se ok
                self.modalNewReservation.courses = data;
                $('#modalNew').modal('show');
                console.log("GetCourses -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                self.modalNewReservation.errorMessage = "Si è verificato un errore";
                $('#modalNew').modal('show');
            });
        },

//selezionata la materia permette di caricarne su timetable le disponibilità
        onSelectCourse: function (e) {
            var codeMat = this.modalNewReservation.courseSelected;
            var idUser = this.modalNewReservation.userSelected;
            var self = this;
            console.log("Selected course code -> " + codeMat);

            if(this.user.role == 'administrator') {
                console.log("Selected user id -> " + idUser);
                if((codeMat == null || codeMat == '-') || (idUser == null || idUser == '-')) {
                    this.modalNewReservation.matrix = [];
                    return;
                }
            }
            else {
                //UTENTE
                if((codeMat == null || codeMat == '-') ) {
                    this.modalNewReservation.matrix = [];
                    return;
                }
            }

            $.get(SERVERURL + 'courseavailability?courseCode=' + codeMat, function (data) {
                console.log("Select availability course -> " + JSON.stringify(data));

                self.modalNewReservation.matrix = data;

            }).fail(function (xhr) {
                console.log("Retrieve course availability " + xhr.status);
                self.modalNewReservation.errorMessage = "Si è verificato un errore";

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

//controlla e gestisce eventuali sovrapposizioni tra nuova prenotazione selezionata e prenotazioni esistenti
        checkFeasibilityNewReservation : function(){
            $('#modalNew').modal('hide');
            console.log("checkFeasibility");
            var j;
            var i;

            this.modalCheckReservation.feedbackMessage=null;
            if(this.modalNewReservation.reservationSelected) {
                if(this.user.role == 'student') {
                    i = this.modalNewReservation.reservationSelected.slot.id - 1;
                    j = this.modalNewReservation.reservationSelected.day.daycode - 1;
                    console.log("checkFeasibility i: "+i+", j: "+j);
                    if (this.lessonsMatrix[i][j].length > 0) {
                        console.log(this.lessonsMatrix[i][j]);
                        this.modalCheckReservation.state = 3;
                        for(var k=0; k<this.lessonsMatrix[i][j].length; k++) {
                            if(this.lessonsMatrix[i][j][k].state.code !== '3') {
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
                this.modalNewReservation.errorMessage="Seleziona un lezione da prenotare o annulla";
            }
        },

        switchTab: function(tab) {
            switch (tab) {
                case 'courses':
                    this.tabActive="courses";
                    this.getCourses(null);
                    break;
                case 'teachers':
                    this.tabActive="teachers";
                    this.getTeachersAdmin(null);
                    break;
                case 'associations':
                    this.tabActive="associations";
                    this.getAssociationsAdmin();
                    break;
                case 'lessons':
                    this.tabActive="lessons";
                    this.getLessonAdmin()
                    break;

            }
        },


//Salvataggio nuova prenotazione su db
        saveNewReservation: function () {
            console.log("saveNewReservation");
            $('#modalCheckFeasibility').modal('hide');
            var self=this;
            this.modalNewReservation.reservationSelected.selected = false;
            $.post(SERVERURL + 'newReservation', {
                infoCatalogItemSelected: JSON.stringify(this.modalNewReservation.reservationSelected),
            }, function (data) {
                console.log("CheckNewReservation -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.showAlert(data.errorMessage);
                } else {
                    $('#modalNew').modal('hide');
                    self.getLessons();
                }
            }).fail(function (xhr) {
                console.log("Save new lesson error code " + xhr.status);
                self.showAlert("Errore nel salvataggio nuova prenotazione  -> status " + xhr.status);

            });
        },

        saveNewReservationAdmin: function () {
            console.log("saveNewReservationAdmin");
            var self=this;
            this.modalNewReservation.reservationSelected.selected = false;
            $.post(SERVERURL + 'newReservation', {
                infoCatalogItemSelected: JSON.stringify(this.modalNewReservation.reservationSelected),
                userId: this.modalNewReservation.userSelected
            }, function (data) {
                console.log("CheckNewReservation -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.showAlert(data.errorMessage);
                } else {
                    $('#modalNew').modal('hide');
                    self.getLessonsAdmin();
                }
            }).fail(function (xhr) {
                console.log("Save new lesson error code " + xhr.status);
                console.log("Save new lesson response text " + xhr.responseText);
                var response = JSON.parse(xhr.responseText);
                self.showAlert(response.errorOccurred);

            });
        },

//Logout utente e ritorno alla home
        logoutAction: function () {
            var self = this;
            $.get(SERVERURL + 'logout', function (data) {
                //se ok
                console.log("Logout -> " + JSON.stringify(data));
                if (data.result == true) {
                    document.location.href = HOMEURL;
                } else {
                    self.showAlert(data.errorMessage);
                }
            }).fail(function (xhr) {
                alert("Errore sul logout -> status " + xhr.status);
            });
        },

        //AREA RISERVATA ADMINISTRATOR
        getCourses: function (callback) {

            var self = this;

            $.get(HOMEURL + 'public/courses?filter=admin', function (data) {
                //se ok
                self.courseAdmin=data;
                console.log("courseadmin -> " + JSON.stringify(data));
                if(callback)
                    callback();


            }).fail(function (xhr) {
                self.showAlert("Errore caricamento corsi -> status " + xhr.status);
            });

        },

        getTeachersAdmin: function (callback) {
            var self=this;
            $.get(HOMEURL + 'public/teachers?filter=admin', function (data) {
                //se ok
                self.teacherAdmin=data;
                console.log("teacheradmin -> " + JSON.stringify(data));
                if(callback)
                    callback();

            }).fail(function (xhr) {
                self.showAlert("Errore caricamento insegnanti -> status " + xhr.status);
            });
        },

        getAssociationsAdmin: function () {
            var self=this;

            $.get(SERVERURL + 'associationsadmin', function (data) {
                //se ok
                self.associationsAdmin=data;
                console.log("courses & teacheradmin -> " + JSON.stringify(data));

            }).fail(function (xhr) {
                self.showAlert("Errore caricamento associazioni docenti e corsi -> status " + xhr.status);
            });
        },

        getLessonAdmin: function () {
            var self=this;


            $.get(SERVERURL + 'lessons', function (data) {
                //se ok
                self.lessonsAdmin=data;
                console.log("Lessons -> " + JSON.stringify(data));

            }).fail(function (xhr) {
                self.showAlert("Errore caricamento delle prenotazioni -> status " + xhr.status);
            });
        },
        getUserAdmin: function (callback) {
            var self = this;
            $.get(SERVERURL + 'userlist', function (data) {
                //se ok
                console.log("user List -> " + JSON.stringify(data));
                self.userAdmin=data;
                if(callback)
                    callback();

            }).fail(function (xhr) {
                self.showAlert("Errore nella lettura lista user -> status " + xhr.status);
            });
        },

        refresh: function(p){
            switch (p) {
                case 1:
                    this.getCourses(null);
                    break;
                case 2:
                    this.getTeachersAdmin(null);
                    break;
                case 3:
                    this.getAssociationsAdmin();
                    break;
                case 4:
                    this.getLessonAdmin();
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
                    this.getUserAdmin(
                        function () {
                            self.modalNewReservation.users = self.userAdmin;

                        }

                    );
                    this.getCourses(
                        function () {
                            self.modalNewReservation.courses=self.courseAdmin;
                            $('#modalNew').modal('show');
                        }
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

            var espression = new RegExp('^[a-z]+$','i');
            e.preventDefault();

            this.modalInsertCourse.errorMessageCode=null;
            this.modalInsertCourse.errorMessageName=null;
            if(this.modalInsertCourse.code==null || this.modalInsertCourse.code.length!=3 || !espression.test(this.modalInsertCourse.code)){
                this.modalInsertCourse.errorMessageCode="Campo obbligatorio di 3 caratteri solo alfanumerici";
                return;
            }

            if(this.modalInsertCourse.name==null|| !espression.test(this.modalInsertCourse.name)){
                this.modalInsertCourse.errorMessageName="Campo obbligatorio. Inserire solo caratteri alfanumerici";
                return;
            }
            else{
                this.insertNewCourseAdmin();
            }

        },
        insertNewCourseAdmin: function (){
            var self=this;

            this.modalInsertCourse.errorMessageServer=null;
            $.post(HOMEURL + 'public/courses', {
                newcode: this.modalInsertCourse.code, newname: this.modalInsertCourse.name, newimage: this.modalInsertCourse.image
            }, function (data) {
                console.log("InsertNewCourse -> " + JSON.stringify(data));
                //se ko
                self.response=data;
                if (!self.response.result) {
                    this.modalInsertCourse.errorMessageServer=self.response.errorOccurred;
                } else {
                    $('#insertCourse').modal('hide');

                    self.getCourses();
                }
            }).fail(function (xhr) {
                console.log("Save new course error code " + xhr.status);
                //alert("Errore nel salvataggio di un nuovo corso  -> status " + xhr.status);
                var response =JSON.parse(xhr.responseText);

                self.modalInsertCourse.errorMessageServer = response.errorOccurred;

            });

        },

        checkInputNewTeacher: function(e){
            e.preventDefault();
            var expressionAlphabetic = new RegExp('^[A-Za-z ]+$','i');
            var expressionNumber=new RegExp('^[0-9]+$','i');
            this.modalInsertTeacher.errorMessageBadge=null;
            this.modalInsertTeacher.errorMessageName=null;
            this.modalInsertTeacher.errorMessageSurname=null;
            this.modalInsertTeacher.errorMessageServer=null;


            if(this.modalInsertTeacher.badge==null || this.modalInsertTeacher.badge.length!=6  || !expressionNumber.test(this.modalInsertTeacher.badge)){
                this.modalInsertTeacher.errorMessageBadge="Campo obbligatorio di 6 caratteri numerici";
                return;
            }

            if(this.modalInsertTeacher.name==null || !expressionAlphabetic.test(this.modalInsertTeacher.name)){
                this.modalInsertTeacher.errorMessageName="Campo obbligatorio. Inserire solo caratteri alfabetici";
                return;
            }
            if(this.modalInsertTeacher.surname==null|| !expressionAlphabetic.test(this.modalInsertTeacher.surname)){
                this.modalInsertTeacher.errorMessageSurname="Campo obbligatorio. Inserire solo caratteri alfabetici";
                return;
            }

            else{
                this.insertNewTeacherAdmin();
            }
        },
        insertNewTeacherAdmin: function (){
            var self=this;

            this.modalInsertTeacher.errorMessageServer=null;
            $.post(HOMEURL + 'public/teachers?filter=admin', {
                newbadge: this.modalInsertTeacher.badge,
                newname: this.modalInsertTeacher.name,
                newsurname: this.modalInsertTeacher.surname,
                newavatar: this.modalInsertTeacher.avatar
            }, function (data) {
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
                console.log("Save new teacher error code " + xhr.status);
                //alert("Errore nel salvataggio di un nuovo corso  -> status " + xhr.status);
                var response =JSON.parse(xhr.responseText);

                self.modalInsertTeacher.errorMessageServer = response.errorOccurred;

            });
        },

        onSelectTeacher: function (e) {
            var badge = e.target.value;
            var self = this;
            if(badge != '-') {
                this.modalInsertAssociation.teacherSelected=badge;
                $.get(SERVERURL + 'courseList?badge=' + badge, function (data) {
                    console.log("Select new course for teacher selected -> " + JSON.stringify(data));

                    self.modalInsertAssociation.courseToMatch = data;

                }).fail(function (xhr) {
                    console.log("Retrieve new course available for teacher " + xhr.status);
                    self.modalInsertAssociation.errorMessage = "Si è verificato un errore";

                });
            }
            console.log("Selected teacher badge -> " + badge);
        },

        saveNewAssociation: function (e) {
            e.preventDefault();
            this.modalInsertAssociation.errorMessageSelectionTeacher=null;
            this.modalInsertAssociation.errorMessageSelectionCourse=null;
            var self=this;
            if(this.modalInsertAssociation.teacherSelected==null || this.modalInsertAssociation.teacherSelected== '-'){
                this.modalInsertAssociation.errorMessageSelectionTeacher="Selezionare un'insegnate";
                return;
            }
            if(this.modalInsertAssociation.courseSelected==null || this.modalInsertAssociation.courseSelected== '-'){
                this.modalInsertAssociation.errorMessageSelectionCourse="Selezionare un corso tra quelli proposti";
                return;
            }
            $.post(SERVERURL + 'courselist', {
                badgeNumber: this.modalInsertAssociation.teacherSelected,
                codCourse: this.modalInsertAssociation.courseSelected
            }, function (data) {
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
                console.log("Save new association teacher - course error code " + xhr.status);
                //alert("Errore nel salvataggio di un nuovo corso  -> status " + xhr.status);
                var response =JSON.parse(xhr.responseText);

                self.modalInsertAssociation.errorMessageServer = response.errorOccurred;

            });
        },
        changeStateAssociation : function (tc) {
            this.modalDeleteAssociation.associationSelected=tc;
            this.modalDeleteAssociation.warningMessage="Attenzione: la cancellazione della associazione " +
                "selezionata potrebbe comportare la cancellazione di prenotazioni effettuate. Vuoi Procedere?"
            $('#deleteAssociation').modal('show');

        },
        saveDeleteAssociation : function () {
            var self=this;
            $.post(SERVERURL + 'deleteassociation', {
                associationToDelete: JSON.stringify(this.modalDeleteAssociation.associationSelected)
            }, function (data) {
                console.log("Delete association -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalDeleteAssociation.errorMessage=data.errorOccurred;
                } else {
                    $('#deleteAssociation').modal('hide');
                    self.getAssociationsAdmin();
                }
            }).fail(function (xhr) {
                console.log("Delete association error code " + xhr.status);
                console.log("Delete association response text " + xhr.responseText);
                var response = JSON.parse(xhr.responseText);
                self.modalDeleteAssociation.errorMessage=response;

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
            $.post(SERVERURL + 'deleteteacher', {
                teacherToDelete: JSON.stringify(this.modalDeleteTeacher.teacherSelected)
            }, function (data) {
                console.log("Delete teacher -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalDeleteTeacher.errorMessage=data.errorOccurred;
                } else {
                    $('#deleteTeacher').modal('hide');
                    self.getTeachersAdmin();
                }
            }).fail(function (xhr) {
                console.log("Delete teacher error code " + xhr.status);
                console.log("Delete teacher response text " + xhr.responseText);
                var response = JSON.parse(xhr.responseText);
                self.modalDeleteTeacher.errorMessage=response;
            });
        }

    }
});






