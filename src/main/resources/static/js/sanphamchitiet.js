let productList = []; // Lưu toàn bộ danh sách sản phẩm
let selectedSize = null;
let selectedColor = null;

// 🚀 Hàm lấy chi tiết sản phẩm
async function fetchProductDetails(productId) {
    try {
        const response = await fetch(`http://localhost:8080/api/san-pham-chi-tiet/${productId}`);
        if (!response.ok) {
            throw new Error(`Lỗi API! Mã trạng thái: ${response.status}`);
        }
        productList = await response.json();

        if (!Array.isArray(productList) || productList.length === 0) {
            throw new Error("Dữ liệu sản phẩm không hợp lệ hoặc rỗng!");
        }

        updateProductDetails(productList);
    } catch (error) {
        console.error('Lỗi khi tải sản phẩm:', error);
    }
}

// 🚀 Cập nhật giao diện sản phẩm
function updateProductDetails(products) {
    const firstProduct = products[0];

    document.querySelector('.product-title').textContent = firstProduct.tenSanPham || "Không có tên sản phẩm";
    document.querySelector('.product-description').textContent = firstProduct.moTa || "Mô tả chưa cập nhật";

    const allImages = [...new Set(products.flatMap(p => p.hinhAnh))];
    document.getElementById('mainImage').src = allImages[0] || "https://via.placeholder.com/300";
    document.querySelector('.thumbnails').innerHTML = allImages.map((img, index) => `
        <img src="${img}" alt="Thumbnail ${index}" onclick="changeImage('${img}')">
    `).join('');

    // 🚀 Hiển thị màu sắc
    const colors = [...new Set(products.map(p => p.tenMauSac))];
    let firstAvailableColor = null;
    const colorOptions = document.getElementById('color-options');
    colorOptions.innerHTML = colors.map((color) => {
        const isDisabled = !products.some(p => p.tenMauSac === color);
        if (!isDisabled && !firstAvailableColor) firstAvailableColor = color;

        return `
            <div class="swatch-1-s color-swatch ${isDisabled ? 'disabled' : ''}" 
                 data-color="${color}">${color}</div>
        `;
    }).join('');

    // 🚀 Hiển thị size
    const sizes = [...new Set(products.flatMap(p => p.sizes))];
    let firstAvailableSize = null;
    const sizeOptions = document.getElementById('size-options');
    sizeOptions.innerHTML = sizes.map((size) => {
        const isDisabled = !products.some(p => p.sizes.includes(size));
        if (!isDisabled && !firstAvailableSize) firstAvailableSize = size;

        return `
            <div class="swatch-1-s size-swatch ${isDisabled ? 'disabled' : ''}" 
                 data-size="${size}">${size}</div>
        `;
    }).join('');

    // 🔥 Gán sự kiện chọn màu bằng addEventListener
    document.querySelectorAll('.color-swatch').forEach(el => {
        el.addEventListener('click', function () {
            if (!this.classList.contains('disabled')) {
                selectColor(this, this.dataset.color);
            }
        });
    });

    // 🔥 Gán sự kiện chọn size bằng addEventListener
    document.querySelectorAll('.size-swatch').forEach(el => {
        el.addEventListener('click', function () {
            if (!this.classList.contains('disabled')) {
                selectSize(this, this.dataset.size);
            }
        });
    });

    selectedColor = null;
    selectedSize = null;

    updatePrice();
}
// 🚀 Chọn màu sắc
// 🚀 Chọn màu sắc
function selectColor(element, color) {
    if (element.classList.contains('disabled')) return;

    // Xóa chọn màu cũ
    document.querySelectorAll('.color-swatch').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    selectedColor = color;

    // Lọc danh sách size hợp lệ cho màu mới
    const availableProducts = productList.filter(p => p.tenMauSac === selectedColor);
    const availableSizes = availableProducts.flatMap(p => p.sizes);

    // Cập nhật danh sách size
    let firstAvailableSize = null;
    document.querySelectorAll('.size-swatch').forEach(el => {
        const size = el.dataset.size;
        if (availableSizes.includes(size)) {
            el.classList.remove('disabled');
            el.addEventListener("click", function () {
                selectSize(el, size);
            });

            if (!firstAvailableSize) firstAvailableSize = size; // Lấy size đầu tiên còn hàng
        } else {
            el.classList.add('disabled');
            el.removeEventListener("click", function () {
                selectSize(el, size);
            });
        }
    });

    // Nếu size cũ không hợp lệ, chọn size đầu tiên còn hàng
    if (!availableSizes.includes(selectedSize)) {
        selectedSize = firstAvailableSize;
    }

    // Cập nhật giao diện size
    document.querySelectorAll('.size-swatch').forEach(el => {
        el.classList.remove('selected');
        if (el.dataset.size === selectedSize) {
            el.classList.add('selected');
        }
    });

    // 🚀 Cập nhật ảnh theo màu sắc
    const colorImages = availableProducts.flatMap(p => p.hinhAnh);
    if (colorImages.length > 0) {
        document.getElementById('mainImage').src = colorImages[0]; // Lấy ảnh đầu tiên
    }

    updatePrice();
}





// 🚀 Chọn size
function selectSize(element, size) {
    if (element.classList.contains('disabled')) return;

    document.querySelectorAll('.size-swatch').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    selectedSize = size;

    updatePrice();
}

// 🚀 Cập nhật giá theo lựa chọn
function updatePrice() {
    const selectedProduct = productList.find(p => p.tenMauSac === selectedColor && p.sizes.includes(selectedSize));

    if (selectedProduct) {
        document.querySelector('.product-price').textContent =
            `${selectedProduct.gia.toLocaleString('vi-VN')} VNĐ`;
    }
}

// 🚀 Thay đổi ảnh chính khi bấm ảnh nhỏ
function changeImage(imageSrc) {
    document.getElementById('mainImage').src = imageSrc;
}

// 🚀 Xử lý số lượng sản phẩm
function increaseQuantity() {
    let quantityInput = document.getElementById('quantity');
    quantityInput.value = parseInt(quantityInput.value) + 1;
}

function decreaseQuantity() {
    let quantityInput = document.getElementById('quantity');
    if (parseInt(quantityInput.value) > 1) {
        quantityInput.value = parseInt(quantityInput.value) - 1;
    }
}

// 🚀 Chạy khi trang tải xong
document.addEventListener("DOMContentLoaded", () => {
    const productId = new URLSearchParams(window.location.search).get('id');
    if (productId) {
        fetchProductDetails(productId);
    }
});

// 🔹 Hàm cập nhật số lượng trên giỏ hàng
function updateCartBadge() {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];
    let totalQuantity = cart.reduce((sum, item) => sum + item.quantity, 0);

    // Cập nhật số trên badge
    document.querySelector(".badge").textContent = totalQuantity;
}

// 🔹 Gọi hàm cập nhật ngay khi tải trang
document.addEventListener("DOMContentLoaded", updateCartBadge);
// 🔹 Thêm vào giỏ hàng
// 🔹 Thêm vào giỏ hàng
document.querySelector(".buy-button").addEventListener("click", function (event) {
    event.preventDefault();

    if (!selectedColor || !selectedSize) {
        Swal.fire({
            icon: "warning",
            title: "Vui lòng chọn màu sắc và size!",
            text: "Bạn cần chọn đầy đủ trước khi thêm vào giỏ hàng.",
            confirmButtonText: "OK"
        });
        return;
    }

    const selectedProduct = productList.find(p => p.tenMauSac === selectedColor && p.sizes.includes(selectedSize));

    if (!selectedProduct) {
        Swal.fire({
            icon: "error",
            title: "Sản phẩm không hợp lệ!",
            text: "Không tìm thấy sản phẩm phù hợp với lựa chọn của bạn.",
            confirmButtonText: "OK"
        });
        return;
    }

    // ✅ Lấy ID sản phẩm chi tiết thay vì ID sản phẩm chung
    const idSanPhamChiTiet = selectedProduct.idSanPhamChiTiet;
    const availableQuantity = selectedProduct.soLuongTheoSize[selectedSize] || 0;

    let quantityInput = document.getElementById('quantity');
    let quantity = parseInt(quantityInput.value);

    if (isNaN(quantity) || quantity <= 0) {
        Swal.fire({
            icon: "warning",
            title: "Số lượng không hợp lệ!",
            text: "Vui lòng nhập số lượng hợp lệ.",
            confirmButtonText: "OK"
        });
        return;
    }

    if (quantity > availableQuantity) {
        Swal.fire({
            icon: "warning",
            title: "Không đủ hàng!",
            text: `Chỉ còn ${availableQuantity} sản phẩm trong kho.`,
            confirmButtonText: "OK"
        });
        return;
    }

    let cart = JSON.parse(localStorage.getItem("cart")) || [];
    let existingProduct = cart.find(item => item.idSanPhamChiTiet === idSanPhamChiTiet);

    if (existingProduct) {
        if (existingProduct.quantity + quantity > availableQuantity) {
            Swal.fire({
                icon: "warning",
                title: "Không thể thêm vào giỏ hàng!",
                html: `
                    <p><strong>Bạn đã có ${existingProduct.quantity} sản phẩm trong giỏ.</strong></p>
                    <p>Số lượng yêu cầu <strong>vượt quá số lượng tồn kho</strong> (<strong>${availableQuantity}</strong>).</p>
                    <p>⚠️ Mong quý khách thông cảm và vui lòng điều chỉnh lại số lượng!</p>
                `,
                confirmButtonText: "OK"
            });
            return;
        }
        existingProduct.quantity += quantity;
    } else {
        cart.push({
            idSanPhamChiTiet: idSanPhamChiTiet,
            name: selectedProduct.tenSanPham,
            price: selectedProduct.gia.toLocaleString('vi-VN'),
            size: selectedSize,
            color: selectedColor,
            image: document.getElementById('mainImage').src,
            quantity: quantity,
            availableQuantity: availableQuantity
        });
    }

    localStorage.setItem("cart", JSON.stringify(cart));
    updateCartBadge();

    Swal.fire({
        icon: "success",
        title: "Thêm vào giỏ hàng thành công!",
        text: "Sản phẩm đã được thêm vào giỏ hàng.",
        confirmButtonText: "Tiếp tục mua sắm"
    });

    console.log("Sản phẩm đã thêm vào giỏ hàng:", {
        idSanPhamChiTiet: idSanPhamChiTiet,
        name: selectedProduct.tenSanPham,
        price: selectedProduct.gia,
        size: selectedSize,
        color: selectedColor,
        image: document.getElementById('mainImage').src,
        quantity: existingProduct ? existingProduct.quantity : quantity
    });

    console.log("Giỏ hàng hiện tại:", JSON.parse(localStorage.getItem("cart")));
});




