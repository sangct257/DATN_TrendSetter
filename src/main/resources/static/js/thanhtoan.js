document.addEventListener("DOMContentLoaded", function () {
    let cartData = sessionStorage.getItem("checkoutCart");
    let orderItemsContainer = document.getElementById("order-items");
    let subtotalElement = document.getElementById("subtotal");
    let totalElement = document.getElementById("total");
    let shippingFee = 30000; // Phí vận chuyển cố định

    if (!cartData) {
        orderItemsContainer.innerHTML = "<p>Giỏ hàng trống.</p>";
        subtotalElement.innerText = "0 VND";
        totalElement.innerText = "0 VND";
        return;
    }

    let cart = JSON.parse(cartData);
    let subtotal = 0;
    orderItemsContainer.innerHTML = "";

    cart.forEach(item => {
        let price = Number(item.price.toString().replace(/\D/g, ""));
        let total = price * item.quantity;
        subtotal += total;

        orderItemsContainer.innerHTML += `
            <div class="d-flex justify-content-between">
                <p>${item.name} (x${item.quantity})</p>
                <p>${total.toLocaleString('vi-VN')} VND</p>
            </div>
        `;
    });

    subtotalElement.innerText = subtotal.toLocaleString('vi-VN') + " VND";
    totalElement.innerText = (subtotal + shippingFee).toLocaleString('vi-VN') + " VND";
});
document.addEventListener("DOMContentLoaded", function () {
    loadProvinces(); // Tải danh sách tỉnh/thành khi trang tải

    async function loadProvinces() {
        let response = await fetch("https://provinces.open-api.vn/api/?depth=1");
        let data = await response.json();
        let provinceSelect = document.getElementById("province");

        data.forEach(province => {
            let option = document.createElement("option");
            option.value = province.code;
            option.textContent = province.name;
            provinceSelect.appendChild(option);
        });
    }
});

async function loadDistricts() {
    let provinceCode = document.getElementById("province").value;
    let districtSelect = document.getElementById("district");
    districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';

    if (!provinceCode) return;

    let response = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
    let data = await response.json();

    data.districts.forEach(district => {
        let option = document.createElement("option");
        option.value = district.code;
        option.textContent = district.name;
        districtSelect.appendChild(option);
    });
}

async function loadWards() {
    let districtCode = document.getElementById("district").value;
    let wardSelect = document.getElementById("ward");
    wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

    if (!districtCode) return;

    let response = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
    let data = await response.json();

    data.wards.forEach(ward => {
        let option = document.createElement("option");
        option.value = ward.code;
        option.textContent = ward.name;
        wardSelect.appendChild(option);
    });
}
function showError(inputId, message) {
    const inputElement = document.getElementById(inputId);
    if (!inputElement) return;
    inputElement.classList.add("is-invalid");

    let oldError = inputElement.nextElementSibling;
    if (oldError && oldError.classList.contains("invalid-feedback")) {
        oldError.remove();
    }

    inputElement.insertAdjacentHTML("afterend", `<div class="invalid-feedback">${message}</div>`);
}

function clearErrors() {
    document.querySelectorAll(".is-invalid").forEach(element => {
        element.classList.remove("is-invalid");
    });
    document.querySelectorAll(".invalid-feedback").forEach(element => {
        element.remove();
    });
}

function validateForm() {
    let isValid = true;
    clearErrors();

    const fields = [
        { id: "fullName", message: "Vui lòng nhập họ và tên." },
        { id: "phoneNumber", message: "Vui lòng nhập số điện thoại hợp lệ.", validate: v => /^\d{10}$/.test(v) },
        { id: "email", message: "Vui lòng nhập email hợp lệ.", validate: v => v.length === 0 || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) },
        { id: "specific-address", message: "Vui lòng nhập địa chỉ cụ thể." },
        { id: "province", message: "Vui lòng chọn Tỉnh/Thành phố." },
        { id: "district", message: "Vui lòng chọn Quận/Huyện." },
        { id: "ward", message: "Vui lòng chọn Phường/Xã." }
    ];

    fields.forEach(field => {
        const value = document.getElementById(field.id)?.value.trim();

        if (!value && field.id !== "email") {
            showError(field.id, field.message);
            isValid = false;
        } else if (field.validate && !field.validate(value)) {
            showError(field.id, field.message);
            isValid = false;
        }
    });

    return isValid;
}


async function placeOrder() {
    if (!validateForm()) {
        return;
    }

    let cartData = localStorage.getItem("cart");
    if (!cartData || JSON.parse(cartData).length === 0) {
        Swal.fire({
            icon: "error",
            title: "Giỏ hàng trống!",
            text: "Vui lòng thêm sản phẩm vào giỏ trước khi đặt hàng."
        });
        return;
    }

    let cart = JSON.parse(cartData);

    // ✅ Giảm số lượng sản phẩm trước khi xác nhận đơn hàng
    for (let item of cart) {
        try {
            let response = await fetch(`/api/san-pham-chi-tiet/reduce-stock/${item.idSanPhamChiTiet}?quantity=${item.quantity}`, {
                method: "POST"
            });

            if (!response.ok) {
                let errorMessage = await response.text();
                Swal.fire({
                    icon: "error",
                    title: "Lỗi khi cập nhật kho!",
                    text: errorMessage
                });
                return; // Nếu có lỗi, dừng việc đặt hàng
            }
        } catch (error) {
            console.error("Lỗi khi giảm số lượng:", error);
            Swal.fire({
                icon: "error",
                title: "Lỗi hệ thống!",
                text: "Không thể giảm số lượng sản phẩm, vui lòng thử lại."
            });
            return;
        }
    }

    // ✅ Nếu cập nhật thành công, tiếp tục xử lý đơn hàng
    let orderInfo = {
        fullName: document.getElementById("fullName").value.trim(),
        phoneNumber: document.getElementById("phoneNumber").value.trim(),
        email: document.getElementById("email").value.trim(),
        addressDetail: document.getElementById("specific-address").value.trim(),
        province: document.getElementById("province").value.trim(),
        district: document.getElementById("district").value.trim(),
        ward: document.getElementById("ward").value.trim(),
        cart: cart,
        paymentMethod: document.getElementById("cod").checked ? "COD" : "Bank Transfer"
    };

    localStorage.setItem("orderInfo", JSON.stringify(orderInfo));

    // ✅ Xóa giỏ hàng sau khi đặt hàng thành công
    localStorage.removeItem("cart");
    sessionStorage.removeItem("checkoutCart");

    if (orderInfo.paymentMethod === "COD") {
        Swal.fire({
            icon: "success",
            title: "Đặt hàng thành công!",
            text: "Bạn có muốn xem đơn hàng của mình không?",
            showCancelButton: true,
            confirmButtonText: "Xem đơn hàng",
            cancelButtonText: "Về trang chủ"
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = "don-hang";
            } else {
                window.location.href = "trang-chu";
            }
        });
    } else {
        window.location.href = "thanh-toan-online";
    }
}



