function openModalForAdd() {
    // Reset modal fields for adding a new address
    document.getElementById('idDiaChi').value = ''; // Clear the hidden address ID
    document.getElementById('diaChiCuThe').value = ''; // Clear the address field
    document.getElementById('thanhPho').value = ''; // Clear the city dropdown
    document.getElementById('huyen').value = ''; // Clear the district dropdown
    document.getElementById('phuong').value = ''; // Clear the ward dropdown

    // Show the modal
    $('#modalDiaChi').modal('show');
}

function saveDiaChi() {
    // Collect the data from the form
    const diaChiCuThe = document.getElementById('diaChiCuThe').value;
    const thanhPho = document.getElementById('thanhPho').value;
    const huyen = document.getElementById('huyen').value;
    const phuong = document.getElementById('phuong').value;

    // Perform your save logic here (e.g., send data to the server or add to the list)
    console.log('Saving new address:', diaChiCuThe, thanhPho, huyen, phuong);

    // After saving, you may close the modal
    $('#modalDiaChi').modal('hide');

    // Optionally, refresh the address list
    // loadDiaChiList();
}
