import Layout from "../components/Layout.vue";
import upload from "../views/upload.vue";
import edit from "../views/edit.vue";
import GalleryList from "../views/GalleryList.vue";
import UploadImage from "../views/UploadImage.vue";
import TagsManage from "../views/TagsManage.vue";

const routes = [
  {
    path: "/",
    component: Layout, // 所有页面都加载侧边导航栏
    redirect: "/upload", // 访问 / 时自动跳转到 /upload
    children: [
      {
        path: "upload",
        name: "Upload",
        component: upload,
      },
      {
        path: "edit",
        name: "Edit",
        component: edit,
      },
      {
        path: "gallery",
        name: "GalleryList",
        component: GalleryList,
      },
      {
        path: "upload-image",
        name: "UploadImage",
        component: UploadImage,
      },
      {
        path: "tag-management",
        name: "TagsManage",
        component: TagsManage,
      },
    ],
  },
];

export default routes;
