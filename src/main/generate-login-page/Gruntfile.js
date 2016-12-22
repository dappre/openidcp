module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.loadNpmTasks('grunt-uncss');
    grunt.loadNpmTasks('grunt-inline');
    grunt.loadNpmTasks('grunt-contrib-htmlmin');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-html');

    grunt.initConfig({
        paths: {
            source: 'src',
            build: 'build'
        },

        pkg: grunt.file.readJSON('package.json'),

        clean: {
            full: ['<%= paths.build %>/**/*'],
            html: ['<%= paths.build %>/**/*.html'],
            css: ['<%= paths.build %>/**/*.css']
        },

        copy: {
            html: {
                expand: true,
                cwd: '<%= paths.source %>',
                src: ['index.html'],
                dest: '<%= paths.build %>/'
            },
            prod: {
                expand: true,
                cwd: '<%= paths.build %>',
                src: ['index.html'],
                dest: '../resources/'
            }
        },

        less: {
            prod: {
                options: {
                    plugins: [
                        new (require('less-plugin-autoprefix'))({browsers: ["last 2 versions"]})
                    ]
                },
                files: {
                    "<%= paths.build %>/styles.css": "<%= paths.source %>/less/styles.less"
                }
            }
        },

        uncss: {
            prod: {
                options: {
                    ignore: [
                            'html.no-desktop .qr-code',
                            'html.no-desktop .login-button' ,

                            'html.no-desktop.ios .toggle-button.button',
                            'html.no-desktop.ios .toggle-button.qrcode',

                            'html.no-desktop.android .toggle-button.button' ,
                            'html.no-desktop.android .toggle-button.qrcode' ,

                            'html.ios .toggle-button.button',
                            'html.android .toggle-button.button'
                            ]
                },
                files: {
                    '<%= paths.build %>/styles.css': ['<%= paths.build %>/index.html']
                }
            }
        },

        inline: {
            prod: {
                options:{
                    cssmin: true,
                    uglify: true
                },
                src: '<%= paths.build %>/index.html',
                dest: '<%= paths.build %>/index.html'
            }
        },

        htmlmin: {
            prod: {
                options: {
                    removeComments: true,
                    collapseWhitespace: true
                },
                files: {
                    '<%= paths.build %>/index.html': '<%= paths.build %>/index.html'
                }
            }
        },

        htmllint: {
            all: '<%= paths.build %>/*.html'
        },

        watch: {
            dev: {
                files: '<%= paths.source %>/**/*.*',
                tasks: ['clean:css', 'copy:html', 'less', 'inline']
            },
            prod: {
                files: '<%= paths.source %>/**/*.*',
                tasks: ['clean:css', 'copy:html', 'less', 'uncss', 'inline', 'clean:css', 'htmlmin', 'copy:prod']
            }
        }
    });


// Tasks
    grunt.registerTask('default',   ['clean:full', 'copy:html', 'less', 'inline', 'watch:dev']);
    grunt.registerTask('prod',      ['clean:full', 'copy:html', 'less', 'uncss', 'inline', 'clean:css', 'htmlmin', 'copy:prod', 'watch:prod']);

};