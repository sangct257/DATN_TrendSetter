document.addEventListener("DOMContentLoaded", function () {
    renderCart();
    console.log("🛒 Giỏ hàng khi tải trang:", JSON.parse(localStorage.getItem("cart")));

    document.getElementById("cart-body").addEventListener("click", function (event) {
        let row = event.target.closest("tr");
        if (!row) return;

        let idSanPhamChiTiet = row.dataset.idsanphamchitiet;
        let size = row.dataset.size;
        let color = row.dataset.color;

        let cart = JSON.parse(localStorage.getItem("cart")) || [];
        let product = cart.find(item =>
            item.idSanPhamChiTiet == idSanPhamChiTiet &&
            item.size == size &&
            item.color == color
        );

        if (!product) return;

        if (event.target.classList.contains("btn-plus")) {
            if (product.quantity < product.availableQuantity) {
                product.quantity += 1;
            } else {
                Swal.fire({
                    icon: "warning",
                    title: "Không đủ hàng!",
                    text: `Chỉ còn ${product.availableQuantity - product.quantity} sản phẩm trong kho.`,
                    confirmButtonText: "OK"
                });
            }
        } else if (event.target.classList.contains("btn-minus")) {
            if (product.quantity > 1) {
                product.quantity -= 1;
            }
        } else if (event.target.classList.contains("btn-remove")) {
            cart = cart.filter(item => !(item.idSanPhamChiTiet == idSanPhamChiTiet && item.size == size && item.color == color));
        }

        localStorage.setItem("cart", JSON.stringify(cart));
        renderCart();
    });

    document.getElementById("checkout-button").addEventListener("click", function () {
        let cart = localStorage.getItem("cart");
        if (cart) {
            sessionStorage.setItem("checkoutCart", cart);
        }
        window.location.href = "thanh-toan";
    });
});

function renderCart() {
    let cartData = localStorage.getItem("cart");
    let cartBody = document.querySelector("#cart-body");
    let subtotal = 0;
    cartBody.innerHTML = "";

    if (!cartData || JSON.parse(cartData).length === 0) {
        cartBody.innerHTML = `<tr><td colspan="7" class="text-center">Giỏ hàng của bạn đang trống.</td></tr>`;
        document.getElementById("subtotal").innerText = "0 VND";
        document.getElementById("total").innerText = "0 VND";
        document.getElementById("shipping-fee").innerText = "0 VND";
        return;
    }

    let cart = JSON.parse(cartData);
    cart.forEach((item, index) => {
        let price = Number(item.price.toString().replace(/\D/g, ""));
        let total = price * item.quantity;
        subtotal += total;

        cartBody.innerHTML += `
<tr data-idsanphamchitiet="${item.idSanPhamChiTiet}" data-size="${item.size}" data-color="${item.color}">
    <td class="align-middle">${index + 1}</td>
    <td class="align-middle">
        <img src="${item.image || 'default.jpg'}" alt="Ảnh sản phẩm" style="width: 50px; height: 50px; object-fit: cover;">
    </td>
    <td class="align-middle">${item.name || "Sản phẩm không xác định"}</td>
    <td class="align-middle">
        Màu: ${item.color || "Không rõ"}, Size: ${item.size || "Không rõ"}
        <br> <small class="text-muted">Tồn kho: ${item.availableQuantity - item.quantity}</small>
    </td>
    <td class="align-middle">
        <div class="input-group quantity mx-auto" style="width: 100px;">
            <div class="input-group-btn">
                <button class="btn btn-sm btn-primary btn-minus"><i class="fa fa-minus"></i></button>
            </div>
            <input type="text" class="form-control form-control-sm bg-secondary text-center quantity-input" value="${item.quantity}" readonly>
            <div class="input-group-btn">
                <button class="btn btn-sm btn-primary btn-plus"><i class="fa fa-plus"></i></button>
            </div>
        </div>
    </td>
    <td class="align-middle">${price.toLocaleString('vi-VN')} VND</td>
    <td class="align-middle">${total.toLocaleString('vi-VN')} VND</td>
    <td class="align-middle">
        <button class="btn btn-sm btn-danger btn-remove"><i class="fa fa-times"></i></button>
    </td>
</tr>
`;
    });

    let shippingFee = cart.length > 0 ? 30000 : 0;
    document.getElementById("shipping-fee").innerText = shippingFee.toLocaleString('vi-VN') + " VND";
    document.getElementById("subtotal").innerText = subtotal.toLocaleString('vi-VN') + " VND";
    document.getElementById("total").innerText = (subtotal + shippingFee).toLocaleString('vi-VN') + " VND";
}
