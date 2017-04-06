$(document).ready(function() {
  $(document).on('click', '#new-transmitter-btn', function() {
    $('#transmitter-form').remove();
    $('body').append('<div class="spinner-overlay"><div class="spinner"></div>');
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
    }).always(function() {
      $('.spinner-overlay').remove();
    });
  });

  $(document).on('click', '#cancel-transmitter-form', function(e) {
    e.preventDefault();
  });

  $(document).on('click', '#cancel-location-form', function(e) {
    e.preventDefault();
  });

  $(document).on('click', '#transmitter-edit-btn', function(e) {
    $('#transmitter-form').remove();
    $('body').append('<div class="spinner-overlay"><div class="spinner"></div>');
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
    }).always(function() {
      $('.spinner-overlay').remove();
    });
  });

  $(document).on('click', '#transmitter-delete-btn', function() {
    var transmitter_id = $(this).data('transmitter-id');

    showDeleteDialog('transmitter', transmitter_id);
  });

  $(document).on('click', '#transmitter-delete-confirmation-btn', function(e) {
    e.preventDefault()
    $('body').append('<div class="spinner-overlay"><div class="spinner"></div>');
    var transmitter_id = $('#transmitter-delete-confirmation input').data('transmitter-delete-id');
    var location_id = location_id = $('#location_id-helper').data('location-id');

    $.ajax({
      url: '/admin/locations/' + location_id + '/transmitters/' + transmitter_id + '/delete',
      type: 'POST',
      success: function(result) {
        window.location.href = '/admin/locations/' + location_id;
      }
    }).always(function() {
      $('.spinner-overlay').remove();
    });
  });

  $(document).on('click', '#location-edit-btn', function() {
    $('body').append('<div class="spinner-overlay"><div class="spinner"></div>');
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
    }).always(function() {
      $('.spinner-overlay').remove();
    });
  });

  $(document).on('click', '#location-delete-btn', function(e) {
    e.preventDefault();
    showDeleteDialog('location');
  })

  $(document).on('click', '#location-delete-confirmation-btn', function(e) {
    e.preventDefault()
    $('body').append('<div class="spinner-overlay"><div class="spinner"></div>');
    var location_id = location_id = $('#location_id-helper').data('location-id');

    $.ajax({
      url: '/admin/locations/' + location_id + '/delete',
      type: 'POST',
      success: function(result) {
        window.location.href = '/admin/locations';
      }
    }).always(function() {
      $('.spinner-overlay').remove();
    });
  });

  var showMessage = function(type, message) {
    $('body').append('<div class="alert alert-' + type + ' error active">' + message + '</div>');
    setTimeout(function() {
      $('.error').removeClass('active');
    }, 3000);
  }

  var showDeleteDialog = function(type, object_id) {
    var modal = '<div class="modal fade" id="' + type + '-delete-confirmation" tabindex="-1" role="dialog"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h4 class="modal-title">Delete confirmation</h4></div<div class="modal-body"><form action="" method="POST"><input type="hidden" data-' + type + '-delete-id="' + object_id + '" /><div>You are going to delete ' + type + '. Procced?</div><br><input type="submit" class="btn btn-success" id="' + type + '-delete-confirmation-btn" value="Continue" name="submit" /><button style="margin-left: 5px;" id="cancel-transmitter-form" class="btn btn-danger" data-toggle="modal" data-target="#' + type + '-delete-confirmation">CANCEL</button></form></div></div></div></div>'

    $('body').append(modal);
    $('#' + type + '-delete-confirmation').modal('toggle');
  }
});
