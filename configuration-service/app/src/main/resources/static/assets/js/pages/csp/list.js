function queryParams() {
    return {
        //type: ['name', 'domain'],
        sort: 'name',
        direction: 'asc',
        per_page: 10,
        page: 1
    };
}

$(document).ready(function() {

    $('body').on('click', 'a.csp-delete', function(e) {
        e.preventDefault();
        var cspId = $(this).attr('data-csp-id');
        var cspLastReport = $(this).attr('data-csp-last-report');
        BootstrapDialog.confirm({
            title: 'Confirmation',
            size: BootstrapDialog.SIZE_NORMAL,
            message: 'Are you sure to delete CSP: <strong>' + cspId + '</strong> and its related information?',
            type: BootstrapDialog.TYPE_WARNING,
            closable: false,
            draggable: true,
            btnCancelLabel: 'Cancel', // <-- Default value is 'Cancel',
            btnOKLabel: 'Confirm',
            btnOKClass: 'btn-warning', // <-- If you didn't specify it, dialog type will be used,
            callback: function(result) {
                // result will be true if button was click, while it will be false if users close the dialog directly.
                if(result) {
                    //Display yet another confirmation
                    BootstrapDialog.confirm({
                        title: 'Confirmation',
                        size: BootstrapDialog.SIZE_NORMAL,
                        message: 'Are you sure to delete CSP: <strong>' + cspId + '</strong> and its related information?<br><br>Last report of CSP was on: <strong>' + cspLastReport + '</strong><br><br>A second confirmation is required!',
                        type: BootstrapDialog.TYPE_DANGER,
                        closable: false,
                        draggable: true,
                        btnCancelLabel: 'Cancel', // <-- Default value is 'Cancel',
                        btnOKLabel: 'Confirm',
                        btnOKClass: 'btn-danger', // <-- If you didn't specify it, dialog type will be used,
                        callback: function(result) {
                            // result will be true if button was click, while it will be false if users close the dialog directly.
                            if(result) {
                                removeCSPrpc(cspId);
                            }else {
                                //just close
                            }
                        }
                    });

                }else {
                    //just close
                }
            }
        });
    });


    function removeCSPrpc(cspId) {
        $.ajax({
            type: 'POST',
            url: REMOVE_URL + "/" + cspId,
            processData: false,
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            success: function (response) {
                console.log(response);

                if (response.responseCode === 0) {
                    setTimeout(function () {
                        $('#csp-table').bootstrapTable('refresh');
                    }, 100);
                }
                else {
                    BootstrapDialog.show({
                        title: 'Error',
                        type: BootstrapDialog.TYPE_DANGER,
                        size: BootstrapDialog.SIZE_SMALL,
                        message: response.responseText,
                        closable: true,
                        closeByBackdrop: false,
                        closeByKeyboard: false,
                        buttons: [{
                            label: 'Close',
                            action: function(dialogRef){
                                dialogRef.close();
                            }
                        }]
                    });
                }
            }
        });
    }
});