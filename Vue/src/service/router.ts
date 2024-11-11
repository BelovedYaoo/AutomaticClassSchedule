import { createRouter, createWebHashHistory } from 'vue-router';
import AppLayout from '@/layout/AppLayout.vue';
import { getParameterByName, globalConfig } from './globalQuote.ts';
import cookie from 'js-cookie';
import { AxiosResponse } from 'axios';
import request from '@/service/request';

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            component: AppLayout,
            children: [
                {
                    path: '/',
                    name: 'dashboard',
                    component: () => import('@/views/Dashboard.vue')
                },
            ]
        },
        {
            path: '/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue')
        },
        {
            path: '/access',
            name: 'accessDenied',
            component: () => import('@/views/pages/Access.vue')
        },
        {
            path: '/error',
            name: 'error',
            component: () => import('@/views/pages/Error.vue')
        },
        {
            path: '/:pathMatch(.*)*',
            name: 'notFound',
            component: () => import('@/views/pages/NotFound.vue')
        }
    ]
});

// 路由守卫
router.beforeEach((to, from, next) => {
    // 判断是否携带授权码
    const code = getParameterByName('code');
    const tokenValue = cookie.get(globalConfig.appTokenName);
    if ((tokenValue === '' || tokenValue === null || tokenValue === undefined) && (code === '' || code === null)) {
        alert('未登录');
        return window.location.href = 'http://openiam.top:9036/#/auth/login?response_type=code&client_id=1001&redirect_uri=http://acs.top:132/';
    } else if (code !== '' && code !== null && code !== undefined) {
        request({
            method: 'GET',
            url: '/codeLogin',
            params: {
                code: code,
            }
        }).then((res: AxiosResponse) => {
            if (res.data.code !== 200 && (tokenValue === '' || tokenValue === null || tokenValue === undefined)) {
                alert('未登录');
                return window.location.href = 'http://openiam.top:9036/#/auth/login?response_type=code&client_id=1001&redirect_uri=http://acs.top:132/';
            }
            console.log('codeLogin:'+res.data.data);
            window.location.href = 'http://acs.top:132/#/';
        });
    }

    next();
});

export default router;
