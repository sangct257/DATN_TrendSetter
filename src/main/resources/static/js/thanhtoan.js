document.addEventListener("DOMContentLoaded", function () {
    validateCartBeforeCheckout();
    loadCheckoutCart();
    loadDiaChi();
    setupAddressDropdownEvents(); // Gắn sự kiện onchange cho các dropdown
});

let showAll = false;
let diaChis = [];
let selectedDiaChiId = null;

let provinceMap = {};
let districtMap = {};

function loadDiaChi() {
    fetch("/api/dia-chi/list", { method: "GET", credentials: "include" })
        .then(response => response.json())
        .then(apiResponse => {
            if (apiResponse && Array.isArray(apiResponse.data) && apiResponse.data.length > 0) {
                diaChis = apiResponse.data;
                const diaChi = diaChis[0];
                console.log("Dữ liệu địa chỉ từ API của bạn:", diaChi);

                document.getElementById("fullName").value = diaChi.hoTen || "N/A";
                document.getElementById("phoneNumber").value = diaChi.soDienThoai || "N/A";
                document.getElementById("email").value = diaChi.email || "N/A";
                document.getElementById("specific-address").value = diaChi.diaChiCuThe || "N/A";

                fillProvinces()
                    .then(() => {
                        const provinceCode = provinceMap[diaChi.thanhPho];
                        if (provinceCode) {
                            setProvince(provinceCode, diaChi.thanhPho);
                            setTimeout(() => {
                                const districtCode = districtMap[diaChi.huyen];
                                if (districtCode) {
                                    setDistrict(districtCode, diaChi.huyen);
                                    setTimeout(() => setWard(diaChi.phuong), 300);
                                } else {
                                    console.warn("Không tìm thấy mã quận/huyện:", diaChi.huyen);
                                }
                            }, 300);
                        } else {
                            console.warn("Không tìm thấy mã tỉnh:", diaChi.thanhPho);
                        }
                    });
            } else {
                loadDiaChiFromGHN();
            }
        })
        .catch(error => {
            console.error("Lỗi khi tải danh sách địa chỉ:", error);
            loadDiaChiFromGHN();
        });
}

function loadDiaChiFromGHN() {
    fetch("https://api.giaohangnhanh.vn/v1/address/default", { method: "GET", credentials: "include" })
        .then(response => response.json())
        .then(ghnResponse => {
            if (ghnResponse && ghnResponse.data) {
                const diaChiGHN = ghnResponse.data;

                console.log("Dữ liệu địa chỉ từ Giao Hàng Nhanh:", diaChiGHN);

                document.getElementById("fullName").value = diaChiGHN.full_name || "N/A";
                document.getElementById("phoneNumber").value = diaChiGHN.phone || "N/A";
                document.getElementById("email").value = diaChiGHN.email || "N/A";
                document.getElementById("specific-address").value = diaChiGHN.specific_address || "N/A";

                fillProvinces()
                    .then(() => {
                        const provinceCode = provinceMap[diaChiGHN.city];
                        if (provinceCode) {
                            setProvince(provinceCode, diaChiGHN.city);
                            setTimeout(() => {
                                const districtCode = districtMap[diaChiGHN.district];
                                if (districtCode) {
                                    setDistrict(districtCode, diaChiGHN.district);
                                    setTimeout(() => setWard(diaChiGHN.ward), 300);
                                } else {
                                    console.warn("Không tìm thấy mã quận/huyện từ GHN:", diaChiGHN.district);
                                }
                            }, 300);
                        } else {
                            console.warn("Không tìm thấy mã tỉnh từ GHN:", diaChiGHN.city);
                        }
                    });
            } else {
                // Nếu không có địa chỉ GHN, chỉ load Tỉnh để người dùng tự chọn
                fillProvinces();
            }
        })
        .catch(error => {
            console.error("Lỗi khi tải địa chỉ từ Giao Hàng Nhanh:", error);
            fillProvinces(); // fallback để user tự chọn
        });
}

function fillProvinces() {
    return fetch("https://provinces.open-api.vn/api/?depth=1")
        .then(response => response.json())
        .then(provinces => {
            const provinceSelect = document.getElementById("province");
            provinceSelect.innerHTML = "<option value=''>Chọn Tỉnh/Thành phố</option>";
            provinces.forEach(province => {
                const option = document.createElement("option");
                option.value = province.code;
                option.textContent = province.name;
                provinceSelect.appendChild(option);
                provinceMap[province.name] = province.code;
            });

            // Cho phép người dùng chọn
            provinceSelect.disabled = false;
            document.getElementById("district").disabled = false;
            document.getElementById("ward").disabled = false;
        });
}

function setProvince(provinceCode, provinceName) {
    console.log("Chọn tỉnh:", provinceName);
    document.getElementById("province").value = provinceCode;
    loadDistrictsByProvince(provinceCode);
}

function loadDistrictsByProvince(provinceCode) {
    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");

    districtSelect.innerHTML = "<option value=''>Chọn Quận/Huyện</option>";
    wardSelect.innerHTML = "<option value=''>Chọn Phường/Xã</option>";
    districtMap = {};

    fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`)
        .then(response => response.json())
        .then(provinceData => {
            provinceData.districts.forEach(district => {
                const option = document.createElement("option");
                option.value = district.code;
                option.textContent = district.name;
                districtSelect.appendChild(option);
                districtMap[district.name] = district.code;
            });
        })
        .catch(error => console.error("Lỗi khi tải danh sách quận/huyện:", error));
}

function setDistrict(districtCode, districtName) {
    console.log("Chọn quận/huyện:", districtName);
    document.getElementById("district").value = districtCode;
    loadWardsByDistrict(districtCode);
}

function loadWardsByDistrict(districtCode) {
    const wardSelect = document.getElementById("ward");
    wardSelect.innerHTML = "<option value=''>Chọn Phường/Xã</option>";

    fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`)
        .then(response => response.json())
        .then(districtData => {
            districtData.wards.forEach(ward => {
                const option = document.createElement("option");
                option.value = ward.name;
                option.textContent = ward.name;
                wardSelect.appendChild(option);
            });
        })
        .catch(error => console.error("Lỗi khi tải danh sách phường/xã:", error));
}

function setWard(wardName) {
    console.log("Chọn phường/xã:", wardName);
    const wardSelect = document.getElementById("ward");
    const options = wardSelect.getElementsByTagName("option");
    for (let option of options) {
        if (option.value.toLowerCase().trim() === wardName.toLowerCase().trim()) {
            option.selected = true;
            break;
        }
    }
}

// Gắn sự kiện onchange cho dropdown
function setupAddressDropdownEvents() {
    const provinceSelect = document.getElementById("province");
    const districtSelect = document.getElementById("district");

    provinceSelect.addEventListener("change", function () {
        const provinceCode = this.value;
        if (provinceCode) {
            loadDistrictsByProvince(provinceCode);
        }
    });

    districtSelect.addEventListener("change", function () {
        const districtCode = this.value;
        if (districtCode) {
            loadWardsByDistrict(districtCode);
        }
    });
}


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

// document.addEventListener("DOMContentLoaded", function () {
//     loadProvinces(); // Tải danh sách tỉnh/thành khi trang tải
//     renderDiaChiCards(data)
// });
function validateForm() {
    clearErrors();
    let isValid = true;

    let requiredFields = [
        { id: "fullName", message: "Vui lòng nhập họ tên." },
        { id: "phoneNumber", message: "Vui lòng nhập số điện thoại." },
        { id: "specific-address", message: "Vui lòng nhập địa chỉ cụ thể." },
        { id: "province", message: "Vui lòng chọn tỉnh/thành phố." },
        { id: "district", message: "Vui lòng chọn quận/huyện." },
        { id: "ward", message: "Vui lòng chọn phường/xã." }
    ];

    requiredFields.forEach(field => {
        let element = document.getElementById(field.id);
        if (!element || element.value.trim() === "") {
            showError(field.id, field.message);
            isValid = false;
        }
    });

    // Kiểm tra email (có thể để trống nhưng nếu nhập phải hợp lệ)
    let emailField = document.getElementById("email");
    if (emailField.value.trim() !== "" && !isValidEmail(emailField.value)) {
        showError("email", "Vui lòng nhập email hợp lệ.");
        isValid = false;
    }

    return isValid;
}

// ⚠️ Hiển thị lỗi bên dưới input
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

// ✅ Xóa lỗi khi người dùng nhập lại
function clearErrors() {
    document.querySelectorAll(".is-invalid").forEach(element => {
        element.classList.remove("is-invalid");
    });
    document.querySelectorAll(".invalid-feedback").forEach(element => {
        element.remove();
    });
}

// 📧 Hàm kiểm tra email hợp lệ
function isValidEmail(email) {
    let regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

// 📌 Xử lý đặt hàng
async function placeOrder() {
    if (!validateForm()) return;

    let cartData = localStorage.getItem("cart");
    if (!cartData || JSON.parse(cartData).length === 0) {
        Swal.fire({ icon: "error", title: "Giỏ hàng trống!", text: "Vui lòng thêm sản phẩm vào giỏ trước khi đặt hàng." });
        return;
    }

    let cart = JSON.parse(cartData);
    let subtotal = cart.reduce((sum, item) => sum + (Number(item.price.replace(/\D/g, "")) * item.quantity), 0);
    let shippingFee = 30000;
    let discount = 0;
    let discountId = null;

    let discountData = JSON.parse(sessionStorage.getItem("checkoutCart"));
    if (discountData?.discount) {
        discount = Number(discountData.discount.value) || 0;
        discountId = discountData.discount.id || null;
    }

    let totalAmount = subtotal + shippingFee - discount;

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
        idPhuongThucThanhToan: document.getElementById("cod").checked ? 1 : 3,
        idPhieuGiamGia: discountId,
        hoaDonChiTiet: cart.map(item => ({
            idSanPhamChiTiet: item.idSanPhamChiTiet,
            soLuong: item.quantity,
            gia: Number(item.price.replace(/\D/g, ""))
        }))
    };

    try {
        // 🔥 Chuẩn bị danh sách sản phẩm cần giảm số lượng
        let stockUpdates = orderInfo.hoaDonChiTiet.map(item => ({
            idSanPhamChiTiet: item.idSanPhamChiTiet,
            soLuong: item.soLuong
        }));

        // 🔥 Gửi 1 request duy nhất để giảm số lượng nhiều sản phẩm cùng lúc
        let stockResponse = await fetch("/api/san-pham-chi-tiet/reduce-stock", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(stockUpdates)
        });

        let stockResult = await stockResponse.json();

        if (!stockResponse.ok) {
            Swal.fire({ icon: "error", title: "Lỗi giảm số lượng!", text: "Không thể giảm số lượng sản phẩm." });
            return;
        }

        // ✅ Kiểm tra phản hồi từ server
        let failedProducts = Object.entries(stockResult)
            .filter(([_, message]) => message.includes("Không đủ hàng"))
            .map(([productId]) => productId);

        if (failedProducts.length > 0) {
            Swal.fire({
                icon: "error",
                title: "Không đủ hàng!",
                text: `Sản phẩm không đủ hàng: ${failedProducts.join(", ")}`
            });
            return;
        }

        // ✅ Tạo hóa đơn
        let response = await fetch("/api/hoa-don/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(orderInfo)
        });

        let result = await response.json();

        if (!response.ok) {
            Swal.fire({ icon: "error", title: "Lỗi tạo hóa đơn!", text: result.message || "Không thể tạo hóa đơn." });
            return;
        }

        // 🔄 Lưu order tạm (nếu cần thiết)
        sessionStorage.setItem("pendingOrder", JSON.stringify(orderInfo));

        // 🔹 Nếu thanh toán COD → thông báo thành công
        if (orderInfo.idPhuongThucThanhToan === 1) {
            localStorage.removeItem("cart");
            sessionStorage.removeItem("checkoutCart");
            sessionStorage.removeItem("discountCode");
            localStorage.removeItem("discountCode");

            Swal.fire({
                icon: "success",
                title: "Đặt hàng thành công!",
                text: `Mã hóa đơn của bạn: ${result.maHoaDon}`,
                showCancelButton: true,
                confirmButtonText: "Xem đơn hàng",
                cancelButtonText: "Về trang chủ"
            }).then((res) => {
                if (res.isConfirmed) {
                    window.location.href = `/don-hang?maHoaDon=${result.maHoaDon}`;
                } else {
                    window.location.href = "/trang-chu";
                }
            });
        }
        // 🔹 Nếu thanh toán VNPay → tạo thanh toán và chuyển hướng
        else {
            sessionStorage.setItem("pendingOrder", JSON.stringify(orderInfo));

            let vnpayResponse = await fetch("/api/payment/create-payment", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    orderId: result.maHoaDon,
                    amount: totalAmount
                })
            });

            let vnpayData = await vnpayResponse.json();

            if (vnpayResponse.ok) {
                window.location.href = vnpayData.paymentUrl;
            } else {
                Swal.fire({ icon: "error", title: "Lỗi VNPay!", text: vnpayData.message || "Không thể tạo thanh toán VNPay." });
            }
        }

    } catch (error) {
        Swal.fire({ icon: "error", title: "Lỗi hệ thống!", text: "Không thể kết nối đến máy chủ." });
    }
}
fetch("/auth")
    .then(response => response.text())
    .then(html => {
        document.getElementById("authContainer").innerHTML = html;
    })
    .catch(error => console.error("Lỗi tải auth.html:", error));

// Hàm mở modal với tab được chọn
function openAuthModal(authType = "login") {
    // Kiểm tra xem modal đã được tải vào DOM chưa
    if (!document.getElementById("authModal")) {
        console.error("Modal chưa tải xong. Chờ thêm 100ms...");
        setTimeout(() => openAuthModal(authType), 100); // Gọi lại sau 100ms
        return;
    }

    if (authType === "register") {
        document.querySelector("#register-tab").click();
    } else {
        document.querySelector("#login-tab").click();
    }

    new bootstrap.Modal(document.getElementById("authModal")).show();
}

