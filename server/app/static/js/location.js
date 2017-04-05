$(document).ready(function() {
  $(document).on('click', '#new-transmitter-btn', function() {
    $('#transmitter-form').remove()
    var location_id = $('#location_id-helper').data('location-id');

    $.ajax({
      type: 'GET',
      url: '/admin/locations/' + location_id + '/transmitters/add',
      success: function(result) {
        $('body').append(result);
        $('#transmitter-form').modal('toggle');
      },
      error: function() {
        showMessage('danger', 'Cannot get new transmitter form.');
      }
    });
  });

  $(document).on('click', '#cancel-transmitter-form', function(e) {
    e.preventDefault();
  });

  $(document).on('click', '#cancel-location-form', function(e) {
    e.preventDefault();
  });

  $(document).on('click', '#transmitter-edit-btn', function(e) {
    $('#transmitter-form').remove()
    var transmitter_id = $(this).data('transmitter-id');
    var location_id = location_id = $('#location_id-helper').data('location-id');

    $.ajax({
      url: '/admin/locations/' + location_id + '/transmitters/' + transmitter_id + '/edit',
      type: 'GET',
      success: function(result) {
        $('body').append(result);
        $('#transmitter-form').modal('toggle');
      },
      error: function() {
        showMessage('danger', 'Cannot get transmitter edit form.');
      }
    });
  });

  $(document).on('click', '#transmitter-delete-btn', function() {
    var transmitter_id = $(this).data('transmitter-id');
    var location_id = location_id = $('#location_id-helper').data('location-id');

    $.ajax({
      url: '/admin/locations/' + location_id + '/transmitters/' + transmitter_id + '/delete',
      type: 'POST',
      success: function(result) {
        $('#' + transmitter_id).closest('div').closest('li').prev('hr').remove()
        $('#' + transmitter_id).closest('div').closest('li').remove();
      }
    });
  });

  $(document).on('click', '#location-edit-btn', function() {
    $('#location-form').remove();
    var location_id = $('#location_id-helper').data('location-id');

    $.ajax({
      url: '/admin/locations/' + location_id + '/edit',
      type: 'GET',
      success: function(result) {
        $('body').append(result);
        $('#location-form').modal('toggle');
      },
      error: function() {
        showMessage('danger', 'Cannot get location edit form.');
      }
    });
  });

  var showMessage = function(type, message) {
    $('body').append('<div class="alert alert-' + type + ' error active">' + message + '</div>');
    setTimeout(function() {
      $('.error').removeClass('active');
    }, 3000);
  }
});
