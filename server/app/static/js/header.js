$(document).ready(function() {
  $(document).on('click', '#create-new-location-btn', function(e) {
    e.preventDefault();
    $('#location-form').remove();
    $('body').append('<div class="spinner-overlay"><div class="spinner"></div>');

    $.ajax({
      type: 'GET',
      url: '/admin/locations/add',
      success: function(result) {
        $('body').append(result);
        $('#location-form').modal('toggle');
      },
      error: function() {
        showMessage('danger', 'Cannot get new location form.');
      }
    }).always(function() {
      $('.spinner-overlay').remove();
    });
  });

  $(document).on('click', '#cancel-location-form', function(e) {
    e.preventDefault();
  });
});
