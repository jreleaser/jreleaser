{"query":{{#f_json}}mutation {
  createPost(
    input: {
      projectId: "{{ Env.NOTICEABLE_PROJECTID }}"
      author: { fullName: "Andres Almiray", email: "{{ Env.NOTICEABLE_AUTHOR_EMAIL}}", avatar: "{{#f_escape_json}}https://firebasestorage.googleapis.com/v0/b/noticeable-service.appspot.com/o/users%2FH430BACfO4PbfXfiCqVEQems6gq2%2Favatars%2F70zOSc8WQijyhbzMYB9r?alt=media&token=522ba931-7bd3-4581-a15e-fe12d32aaee3{{/f_escape_json}}" }
      content: "{{#f_escape_json}}{{#f_recursive_eval}}{{#f_file_read}}{{basedir}}/src/jreleaser/templates/http/noticeable_announcement.tpl{{/f_file_read}}{{/f_recursive_eval}}{{/f_escape_json}}"
      forwardToWebhooks: false
      isDraft: false
      labels: [
        { name: "New", slug: "new", textColor: "#ffffff", backgroundColor: "#f3322f" },
        { name: "Announcement", slug: "announcement", textColor: "#ffffff", backgroundColor: "#6b6cf3" }
      ]
      publicationTime: "{{#f_now_gmt}}yyyy-MM-dd'T'hh:mm:ss'Z'{{/f_now_gmt}}"
      title: "{{releaseName}}"
    }
  ) {
    post {
      id
      permalink
    }
  }
}{{/f_json}}}
