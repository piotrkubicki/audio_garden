$(document).ready(function() {
  $(document).on('click', '.x-icon', function(e) {
    e.preventDefault();
    location_id = $(this).data('location-id');
    console.log('dsd');
    window.location.href = '/admin/locations/' + location_id + '/delete';
  });
});
