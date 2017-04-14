$(document).ready(function() {
  $(document).on('click', '.locations-delete-btn', function(e) {
    e.preventDefault();
    var location_id = $(this).data('delete-location-id');

    showDeleteDialog('location', location_id);
  });

  $(document).on('click', '#location-delete-confirmation-btn', function(e) {
    e.preventDefault()
    $('body').append('<div class="spinner-overlay"><div class="spinner"></div>');
    var location_id = $('#location-delete-confirmation input').data('location-delete-id');

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

  $(document).on('click', '#location-delete-confirmation', function(e) {
    e.preventDefault();
  });

  $(document).on('click', 'input[type="submit"]', function() {
    $('body').append('<div class="spinner-overlay"><div class="spinner"></div>');
  });

  var showDeleteDialog = function(type, object_id) {
    var modal = '<div class="modal fade" id="' + type + '-delete-confirmation" tabindex="-1" role="dialog"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h4 class="modal-title">Delete confirmation</h4></div<div class="modal-body"><form action="" method="POST"><input type="hidden" data-' + type + '-delete-id="' + object_id + '" /><div>You are going to delete ' + type + '. Procced?</div><br><input type="submit" class="btn btn-success" id="' + type + '-delete-confirmation-btn" value="CONTINUE" name="submit" /><button style="margin-left: 5px;" id="cancel-transmitter-form" class="btn btn-danger" data-toggle="modal" data-target="#' + type + '-delete-confirmation">CANCEL</button></form></div></div></div></div>'

    $('body').append(modal);
    $('#' + type + '-delete-confirmation').modal('toggle');
  }
});
