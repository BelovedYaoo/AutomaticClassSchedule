import globals from 'globals';
import tseslint from 'typescript-eslint';
import pluginVue from 'eslint-plugin-vue';

export default [
    {
        files: ['**/*.{js,mjs,cjs,ts,vue}']
    },
    {
        languageOptions: {
            globals: globals.browser
        }
    },
    ...tseslint.configs.recommended,
    ...pluginVue.configs['flat/essential'],
    {
        files: ['**/*.vue'],
        languageOptions: {
            parserOptions: {
                parser: tseslint.parser
            }
        }
    },
    {
        rules: {
            'vue/multi-word-component-names': 'off',
            'vue/no-reserved-component-names': 'off',
            'vue/component-tags-order': [
                'error',
                {
                    order: ['script', 'template', 'style']
                }
            ],
            // 类型转换比较检查，禁止使用强制类型转换比较
            'eqeqeq': ['error', 'always'],
            // 行尾空格检查，禁止使用行尾空格
            'no-trailing-spaces': 'error',
            // 字符串拼接检查，禁止使用字符串拼接
            'prefer-template': 'error',
            // 连续空行检查，禁止使用超过1行的连续空行
            'no-multiple-empty-lines': ['error', {
                'max': 1
            }],
            // 未使用变量检查，禁止使用未使用的变量
            'no-unused-vars': ['error', {
                'args': 'none',
                'vars': 'all',
                'varsIgnorePattern': '^_'
            }],
            // 对象属性简写检查，禁止使用对象属性简写
            'object-shorthand': ['error', 'never'],
            // require检查，禁止使用require
            '@typescript-eslint/no-require-imports': 'error',
            // 引号检查，引号必须使用单引号
            'quotes': ['error', 'single'],
            // 声明检查，从未覆写的变量必须使用const
            'prefer-const': ['error', {
                'destructuring': 'any',
                'ignoreReadBeforeAssign': true
            }],
            // 驼峰命名检查，命名必须使用驼峰命名
            'camelcase': ['error', {
                'properties': 'always',
                'ignoreGlobals': false
            }],
            // 行末尾分号检查，行尾必须添加分号
            'semi': ['error', 'always'],
            // console检查，不建议使用console.log输出，但允许console.warn和console.error
            'no-console': ['warn', {
                'allow': ['warn', 'error']
            }],
            // any类型检查，使用any类型不再警告
            '@typescript-eslint/no-explicit-any': ['off']
        }
    }
];