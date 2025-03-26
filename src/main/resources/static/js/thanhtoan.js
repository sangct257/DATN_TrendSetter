document.addEventListener("DOMContentLoaded", function () {
    validateCartBeforeCheckout();
    loadCheckoutCart();
});

function validateCartBeforeCheckout() {
    let currentCart = JSON.parse(localStorage.getItem("cart")) || [];

    if (currentCart.length === 0) {
        console.log("🛒 Giỏ hàng trống! Xoá dữ liệu thanh toán...");
        sessionStorage.removeItem("checkoutCart"); // Xoá dữ liệu cũ
    }
}

function loadCheckoutCart() {
    let checkoutData = JSON.parse(sessionStorage.getItem("checkoutCart"));

    if (!checkoutData || !checkoutData.cart || checkoutData.cart.length === 0) {
        document.getElementById("order-items").innerHTML = "<p>Không có sản phẩm nào trong giỏ hàng.</p>";
        document.getElementById("subtotal").innerText = "0 VND";
        document.getElementById("shipping-fee").innerText = "0 VND";
        document.getElementById("total").innerText = "0 VND";
        document.getElementById("discount-container").style.display = "none";
        return;
    }

    let cart = checkoutData.cart;
    let orderItemsContainer = document.getElementById("order-items");
    let subtotal = 0;

    orderItemsContainer.innerHTML = "";

    cart.forEach((item) => {
        let price = Number(item.price.toString().replace(/\D/g, ""));
        let total = price * item.quantity;
        subtotal += total;

        orderItemsContainer.innerHTML += `
            <div class="d-flex justify-content-between">
                <p>${item.name} (${item.size}, ${item.color}) x ${item.quantity}</p>
                <p>${total.toLocaleString('vi-VN')} VND</p>
            </div>
        `;
    });

    let shippingFee = Number(checkoutData.shippingFee.replace(/\D/g, ""));
    let discount = checkoutData.discount.value || 0;
    let totalAmount = subtotal + shippingFee - discount;

    document.getElementById("subtotal").innerText = `${subtotal.toLocaleString('vi-VN')} VND`;
    document.getElementById("shipping-fee").innerText = `${shippingFee.toLocaleString('vi-VN')} VND`;
    document.getElementById("total").innerHTML = `<span style="color: red; font-weight: bold;">${totalAmount.toLocaleString('vi-VN')} VND</span>`;

    if (discount > 0) {
        document.getElementById("discount-container").style.display = "flex";
        document.getElementById("discount-display").innerText = `-${discount.toLocaleString('vi-VN')} VND`;
    } else {
        document.getElementById("discount-container").style.display = "none";
    }
}



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

async function placeOrder() {
    console.log("🔹 [DEBUG] Bắt đầu đặt hàng...");

    //
    // if (!validateForm()) {
    //     console.log("❌ [DEBUG] Form không hợp lệ!");
    //     return;
    // }

    let cartData = localStorage.getItem("cart");
    if (!cartData || JSON.parse(cartData).length === 0) {
        console.log("❌ [DEBUG] Giỏ hàng trống!");
        Swal.fire({
            icon: "error",
            title: "Giỏ hàng trống!",
            text: "Vui lòng thêm sản phẩm vào giỏ trước khi đặt hàng."
        });
        return;
    }

    let cart = JSON.parse(cartData);
    console.log("🛒 [DEBUG] Dữ liệu giỏ hàng:", cart);

    // ✅ Chuyển đổi giá từ chuỗi thành số trước khi tính toán
    let subtotal = cart.reduce((sum, item) => {
        let price = Number(item.price.toString().replace(/\D/g, "")); // Chuyển "450.000" -> 450000
        return sum + (price * item.quantity);
    }, 0);

    let shippingFee = 30000; // Giả sử phí ship là 30,000 VND

    // ✅ Kiểm tra mã giảm giá
    let discount = 0;
    let discountId = null;
    let discountData = JSON.parse(sessionStorage.getItem("checkoutCart"));
    if (discountData && discountData.discount) {
        discount = Number(discountData.discount.value) || 0;
        discountId = discountData.discount.id || null;
    }

    let totalAmount = subtotal + shippingFee - discount;

    console.log("💰 [DEBUG] Tổng tiền hàng:", subtotal.toLocaleString('vi-VN'), "VND");
    console.log("🚚 [DEBUG] Phí vận chuyển:", shippingFee.toLocaleString('vi-VN'), "VND");
    console.log("🎁 [DEBUG] Giảm giá:", discount.toLocaleString('vi-VN'), "VND");
    console.log("💵 [DEBUG] Tổng tiền cuối cùng:", totalAmount.toLocaleString('vi-VN'), "VND");

    // ✅ Chuẩn bị dữ liệu để gửi lên API
    let orderInfo = {
        nguoiNhan: document.getElementById("fullName").value.trim(),
        soDienThoai: document.getElementById("phoneNumber").value.trim(),
        email: document.getElementById("email").value.trim(),
        diaChiCuThe: document.getElementById("specific-address").value.trim(),
        thanhPho: document.getElementById("province").options[document.getElementById("province").selectedIndex].text,
        huyen: document.getElementById("district").options[document.getElementById("district").selectedIndex].text,
        phuong: document.getElementById("ward").options[document.getElementById("ward").selectedIndex].text,
        tongTien: totalAmount,
        phiShip: shippingFee,
        idPhuongThucThanhToan: document.getElementById("cod").checked ? 1 : 2, // 1: COD, 2: Chuyển khoản
        idPhieuGiamGia: discountId, // ✅ Gửi mã giảm giá nếu có
        hoaDonChiTiet: cart.map(item => ({
            idSanPhamChiTiet: item.idSanPhamChiTiet,
            soLuong: item.quantity,
            gia: Number(item.price.toString().replace(/\D/g, "")) // Chuyển đổi giá chính xác
        }))
    };
    console.log("📦 [DEBUG] Dữ liệu gửi lên API hóa đơn:", JSON.stringify(orderInfo, null, 2));
    console.log("📦 [DEBUG] Dữ liệu gửi lên API hóa đơn:", orderInfo);

    try {
        let response = await fetch("/api/hoa-don/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(orderInfo)
        });

        let result = await response.json();

        if (response.ok) {
            console.log("✅ [DEBUG] Đặt hàng thành công! ID hóa đơn:", result.id);
            console.log("📜 [DEBUG] Mã hóa đơn:", result.maHoaDon);

            // ✅ Xóa giỏ hàng sau khi đặt hàng thành công
            localStorage.removeItem("cart");
            sessionStorage.removeItem("checkoutCart");

            Swal.fire({
                icon: "success",
                title: "Đặt hàng thành công!",
                text: `Mã hóa đơn của bạn: ${result.maHoaDon}`,
                showCancelButton: true,
                confirmButtonText: "Xem đơn hàng",
                cancelButtonText: "Về trang chủ"
            }).then((res) => {
                if (res.isConfirmed) {
                    window.location.href = "/don-hang";
                } else {
                    window.location.href = "/";
                }
            });
        } else {
            console.error("❌ [ERROR] Lỗi từ API hóa đơn:", result.message);
            Swal.fire({
                icon: "error",
                title: "Lỗi đặt hàng!",
                text: result.message || "Đã có lỗi xảy ra, vui lòng thử lại."
            });
        }
    } catch (error) {
        console.error("❌ [ERROR] Lỗi hệ thống khi gửi hóa đơn:", error);
        Swal.fire({
            icon: "error",
            title: "Lỗi hệ thống!",
            text: "Không thể kết nối đến máy chủ, vui lòng thử lại sau."
        });
    }
}

