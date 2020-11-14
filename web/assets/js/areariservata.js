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
            errorMessage: null,
        },
        modalNewReservation : {
            courses: [],
            matrix: null,
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
        associationsAdmin: [],
        lessonsAdmin: [],
        tabActive: null,

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
            var code = e.target.value;
            var self = this;
            $.get(SERVERURL + 'courseavailability?courseCode=' + code, function (data) {
                console.log("Select availability course -> " + JSON.stringify(data));

                self.modalNewReservation.matrix = data;

            }).fail(function (xhr) {
                console.log("Retrieve course availability " + xhr.status);
                self.modalNewReservation.errorMessage = "Si è verificato un errore";

            });
            console.log("Selected course code -> " + code);

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
            }else{
                this.modalNewReservation.errorMessage="Seleziona un lezione da prenotare o annulla";
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
                    alert(data.errorMessage);
                } else {
                    $('#modalNew').modal('hide');
                    self.getLessons();
                }
            }).fail(function (xhr) {
                console.log("Save new lesson error code " + xhr.status);
                alert("Errore nel salvataggio nuova prenotazione  -> status " + xhr.status);

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
                    alert(data.errorMessage);
                }
            }).fail(function (xhr) {
                alert("Errore sul logout -> status " + xhr.status);
            });
        },

        //AREA RISERVATA ADMINISTRATOR
        getCourses: function () {
            this.tabActive = "courses";
            var self = this;

            $.get(HOMEURL + 'public/courses?filter=admin', function (data) {
                //se ok
                self.courseAdmin=data;
                console.log("courseadmin -> " + JSON.stringify(data));


            }).fail(function (xhr) {
                alert("Errore caricamento corsi -> status " + xhr.status);
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
                alert("Errore caricamento insegnanti -> status " + xhr.status);
            });
        },

        getAssociationsAdmin: function () {
            var self=this;
            this.tabActive = "associations";
            $.get(SERVERURL + 'associationsadmin', function (data) {
                //se ok
                self.associationsAdmin=data;
                console.log("courses & teacheradmin -> " + JSON.stringify(data));

            }).fail(function (xhr) {
                alert("Errore caricamento associazioni docenti e corsi -> status " + xhr.status);
            });
        },

        getLessonAdmin: function () {
            var self=this;
            this.tabActive = "lessons";

            $.get(SERVERURL + 'lessons', function (data) {
                //se ok
                self.lessonsAdmin=data;
                console.log("Lessons -> " + JSON.stringify(data));

            }).fail(function (xhr) {
                alert("Errore caricamento delle prenotazioni -> status " + xhr.status);
            });
        },

        refresh: function(p){
            switch (p) {
                case 1:
                    this.getCourses();
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
                    $('#insertNewReservation').modal('show');
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
        }

    }
});






