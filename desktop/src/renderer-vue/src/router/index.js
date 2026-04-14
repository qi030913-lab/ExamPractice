import { createRouter, createWebHashHistory } from "vue-router";
import { useSessionStore } from "@/stores/session";
import ShellLayout from "@/layouts/ShellLayout.vue";
import LoginView from "@/views/LoginView.vue";
import HomeView from "@/views/HomeView.vue";
import TeacherWorkbenchView from "@/views/teacher/TeacherWorkbenchView.vue";
import TeacherPapersView from "@/views/teacher/TeacherPapersView.vue";
import TeacherPaperDetailView from "@/views/teacher/TeacherPaperDetailView.vue";
import TeacherStudentsView from "@/views/teacher/TeacherStudentsView.vue";
import TeacherStudentDetailView from "@/views/teacher/TeacherStudentDetailView.vue";
import TeacherRecordDetailView from "@/views/teacher/TeacherRecordDetailView.vue";
import TeacherImportView from "@/views/teacher/TeacherImportView.vue";
import StudentWorkbenchView from "@/views/student/StudentWorkbenchView.vue";
import StudentPapersView from "@/views/student/StudentPapersView.vue";
import StudentRecordsView from "@/views/student/StudentRecordsView.vue";
import StudentRecordDetailView from "@/views/student/StudentRecordDetailView.vue";
import StudentExamView from "@/views/student/StudentExamView.vue";
import StudentExamResultView from "@/views/student/StudentExamResultView.vue";

const routes = [
  {
    path: "/",
    component: ShellLayout,
    children: [
      {
        path: "",
        redirect: "/login"
      },
      {
        path: "login",
        name: "login",
        component: LoginView
      },
      {
        path: "home",
        name: "home",
        component: HomeView
      },
      {
        path: "teacher",
        name: "teacher-workbench",
        component: TeacherWorkbenchView
      },
      {
        path: "teacher/papers",
        name: "teacher-papers",
        component: TeacherPapersView
      },
      {
        path: "teacher/papers/:paperId",
        name: "teacher-paper-detail",
        component: TeacherPaperDetailView
      },
      {
        path: "teacher/import",
        name: "teacher-import",
        component: TeacherImportView
      },
      {
        path: "teacher/students",
        name: "teacher-students",
        component: TeacherStudentsView
      },
      {
        path: "teacher/students/:studentId",
        name: "teacher-student-detail",
        component: TeacherStudentDetailView
      },
      {
        path: "teacher/students/:studentId/records/:recordId",
        name: "teacher-record-detail",
        component: TeacherRecordDetailView
      },
      {
        path: "student",
        name: "student-workbench",
        component: StudentWorkbenchView
      },
      {
        path: "student/papers",
        name: "student-papers",
        component: StudentPapersView
      },
      {
        path: "student/papers/:paperId/exam",
        name: "student-exam",
        component: StudentExamView
      },
      {
        path: "student/records",
        name: "student-records",
        component: StudentRecordsView
      },
      {
        path: "student/records/:recordId",
        name: "student-record-detail",
        component: StudentRecordDetailView
      },
      {
        path: "student/records/:recordId/result",
        name: "student-submit-result",
        component: StudentExamResultView
      },
      {
        path: "student/achievement",
        name: "student-achievement",
        component: () => import("@/views/student/StudentAchievementView.vue")
      }
    ]
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

router.beforeEach(async (to) => {
  const sessionStore = useSessionStore();

  if (to.name === "login" && sessionStore.isLoggedIn) {
    return sessionStore.isTeacher ? "/teacher" : "/student";
  }

  const requiresAuth = [
    "home",
    "teacher-workbench",
    "teacher-papers",
    "teacher-paper-detail",
    "teacher-import",
    "teacher-students",
    "teacher-student-detail",
    "teacher-record-detail",
    "student-workbench",
    "student-papers",
    "student-exam",
    "student-records",
    "student-record-detail",
    "student-submit-result",
    "student-achievement"
  ].includes(String(to.name || ""));

  if (!requiresAuth) {
    return true;
  }

  if (!sessionStore.isLoggedIn) {
    await sessionStore.restoreSession();
  }

  if (!sessionStore.isLoggedIn) {
    return "/login";
  }

  if (!sessionStore.workbench) {
    try {
      await sessionStore.loadWorkbench();
    } catch (_error) {
      sessionStore.logout();
      return "/login";
    }
  }

  const teacherRoutes = [
    "teacher-workbench",
    "teacher-papers",
    "teacher-paper-detail",
    "teacher-import",
    "teacher-students",
    "teacher-student-detail",
    "teacher-record-detail"
  ];
  if (teacherRoutes.includes(String(to.name || "")) && !sessionStore.isTeacher) {
    return "/student";
  }

  if (to.name === "student-workbench" && !sessionStore.isStudent) {
    return "/teacher";
  }

  const studentRoutes = [
    "student-workbench",
    "student-papers",
    "student-exam",
    "student-records",
    "student-record-detail",
    "student-submit-result",
    "student-achievement"
  ];
  if (studentRoutes.includes(String(to.name || "")) && !sessionStore.isStudent) {
    return "/teacher";
  }

  return true;
});

export default router;
