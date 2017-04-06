$(document).ready(function() {
  $(document).on('click', '.locations-delete-btn', function(e) {
    e.preventDefault();
    $.post(this.href);
    window.location.href = '/admin/locations';
  });
});
