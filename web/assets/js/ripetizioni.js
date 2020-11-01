var SERVERURL = window.location.href;


var courses = new Vue({
    el: "#courses",
    data: {
        courses: [],
        link: SERVERURL+"public/courses"
    },
    mounted: function () {
        this.getCourses();
    },
    methods: {
        getCourses: function () {
            var self = this;
            $.get(this.link, function (data) {
                //se ok
                self.courses = data;
                console.log("GetCourses -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                alert("Si è verificato un errore!!!");
            });
        }
    }


});


var teachers = new Vue({
    el: "#teachers",
    data: {
        teachers: [],
        link: SERVERURL+"public/teachers"
    },
    mounted: function () {
        this.getTeachers();
    },
    methods: {
        getTeachers: function () {
            var self = this;
            $.get(this.link, function (data) {
                //se ok
                self.teachers = data;
                console.log("GetTeachers -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                alert("Si è verificato un errore!!!");
            });
        }
    }
});
//login in
var login=new Vue({
    el:"#login",
    data:{
        info:null,
        user:'',
        psw:'',
        link: SERVERURL+"public/login"
    },
    methods:{
        loginaction: function (e) {
            e.preventDefault();
            var self=this;
            this.info = null;
            $.post(this.link, {user:this.user, psw:this.psw}, function (data) {
                console.log("CheckLogin -> " + JSON.stringify(data));
                //se ok
                if(!data.result)
                    self.info=data;
                else {
                    document.location.href= SERVERURL+"private/";

                }

            }).fail(function (xhr) {
                console.log("CheckLogin error code "+xhr.status);
                console.log("CheckLogin error text "+xhr.responseText);
                self.info=JSON.parse(xhr.responseText);
                console.log(self.info);
            });
        }
    }
});






