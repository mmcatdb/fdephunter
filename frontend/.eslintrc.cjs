module.exports = {
    env: {
        browser: true,
        es2020: true,
    },
    extends: [
        'eslint:recommended',
        'plugin:react/recommended',
        'plugin:@typescript-eslint/recommended',
        'plugin:react-hooks/recommended',
    ],
    parser: '@typescript-eslint/parser',
    parserOptions: { ecmaVersion: 'latest', sourceType: 'module' },
    plugins: [
        'react-refresh',
    ],
    rules: {
        'react/react-in-jsx-scope': 'off',
        'react-refresh/only-export-components': 'warn',
        'semi': [ 'error', 'always' ],
        'indent': [ 'warn', 4 ],
        'array-bracket-spacing': [ 'warn', 'always' ],
        'object-curly-spacing': [ 'warn', 'always' ],
        'space-before-function-paren': [ 'warn', {
            'anonymous': 'always',
            'named': 'never',
            'asyncArrow': 'always',
        } ],
        'curly': [ 'warn', 'multi-or-nest', 'consistent' ],
        'brace-style': [ 'warn', 'stroustrup' ],
        '@typescript-eslint/no-empty-function': [ 'warn', { 'allow': [ 'private-constructors' ] } ],
        'react/display-name': [ 'off' ],
        'nonblock-statement-body-position': [ 'error', 'below' ],
        '@typescript-eslint/member-delimiter-style': [ 'error', { 'singleline': { 'delimiter': 'comma' } } ],
        '@typescript-eslint/consistent-type-imports': [ 'error', { 'fixStyle': 'inline-type-imports' } ],
        'comma-dangle': [ 'warn', 'always-multiline' ],
        'react-hooks/exhaustive-deps': [ 'warn' ],
        'quotes': [ 'warn', 'single', { 'allowTemplateLiterals': true } ],
        'jsx-quotes': [ 'warn', 'prefer-single' ],
    },
    settings: {
        react: {
            version: 'detect',
        },
    },
};
