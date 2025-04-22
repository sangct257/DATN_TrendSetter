let currentPage = 0; // Trang hiện tại
const pageSize = 6;  // Số sản phẩm trên mỗi trang
let currentCategory = "";
let allProducts = []; // Toàn bộ sản phẩm lấy được từ API

// Hàm lấy dữ liệu sản phẩm từ API
async function fetchProducts(page = 0) {
    // Lấy danh mục từ sessionStorage (nếu có)
    let danhMuc = sessionStorage.getItem("selectedCategory") || ""; // Lấy danh mục đã lưu từ sessionStorage

    let apiUrl = `http://localhost:8080/api/sanpham?page=${page}`;

    if (danhMuc) {
        currentCategory = danhMuc; // Cập nhật danh mục hiện tại
        apiUrl = `http://localhost:8080/api/sanpham/filter?danh_muc=${encodeURIComponent(danhMuc)}&page=${page}`;
    }

    try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
            throw new Error(`Lỗi API! Mã trạng thái: ${response.status}`);
        }

        const data = await response.json();
        if (!data || !data.content) {
            throw new Error("API không trả về dữ liệu hợp lệ");
        }

        allProducts = data.content; // Lưu lại toàn bộ danh sách sản phẩm
        renderProducts(data.content);
        updatePaginationControls(data.totalPages);
    } catch (error) {
        console.error("Lỗi khi tải sản phẩm:", error);
    }
}

document.querySelectorAll("#price-filter input[type=checkbox]").forEach(checkbox => {
    checkbox.addEventListener("change", filterByPriceFE);
});

function filterByPriceFE() {
    let filteredProducts = allProducts;

    // Nếu checkbox "Tất cả giá" được chọn -> không lọc gì cả
    const allPriceChecked = document.getElementById("price-all").checked;
    if (!allPriceChecked) {
        // Nếu không chọn tất cả -> lọc theo các checkbox đang được tick
        let selectedRanges = [];

        if (document.getElementById("price-1").checked) selectedRanges.push({ min: 0, max: 100000 });
        if (document.getElementById("price-2").checked) selectedRanges.push({ min: 100000, max: 200000 });
        if (document.getElementById("price-3").checked) selectedRanges.push({ min: 200000, max: 300000 });
        if (document.getElementById("price-4").checked) selectedRanges.push({ min: 300000, max: 400000 });
        if (document.getElementById("price-5").checked) selectedRanges.push({ min: 500000, max: Infinity });

        filteredProducts = allProducts.filter(product => {
            return selectedRanges.some(range =>
                product.gia >= range.min && product.gia < range.max
            );
        });
    }

    renderProducts(filteredProducts);
}

// Hàm gọi fetchProducts khi trang được tải lần đầu
document.addEventListener("DOMContentLoaded", function () {
    let danhMuc = sessionStorage.getItem("selectedCategory"); // Lấy danh mục từ sessionStorage

    // Nếu có danh mục trong sessionStorage, chỉ cần hiển thị danh mục đó mà không gọi fetchProducts ngay lập tức
    if (danhMuc) {
        currentCategory = danhMuc; // Cập nhật lại danh mục hiện tại
    } else {
        // Nếu không có danh mục, thì gọi fetchProducts để lấy tất cả sản phẩm
        fetchProducts(currentPage);
    }
});




// Hàm hiển thị danh sách sản phẩm
function renderProducts(products) {
    const productListContainer = document.getElementById('product-list');
    productListContainer.innerHTML = ''; // Xóa sản phẩm cũ

    products.forEach(product => {
        const productElement = document.createElement('div');
        productElement.classList.add('col-lg-4', 'col-md-6', 'col-sm-12', 'pb-1');
        productElement.innerHTML = `
            <div class="card product-item border-0 mb-4" data-id="${product.id}">
                <div class="card-header product-img position-relative overflow-hidden bg-transparent border p-0">
                    <img class="img-fluid w-100" src="${product.urlHinhAnh || 'img/default-product.jpg'}" alt="${product.tenSanPham}">
                </div>
                <div class="card-body border-left border-right text-center p-0 pt-4 pb-3">
                    <h6 class="text-truncate mb-3">${product.tenSanPham}</h6>
                    <div class="d-flex justify-content-center">
                        <h6>${product.gia ? product.gia.toLocaleString('vi-VN') + 'đ' : '0đ'}</h6>
                    </div>
                </div>
                <div class="card-footer d-flex justify-content-between bg-light border">
                    <a href="#" class="btn btn-sm text-dark p-0 quick-view-btn text-center w-100" data-id="${product.id}">
                        <i class="fas fa-eye text-primary mr-1"></i>Xem nhanh
                    </a>
                </div>
            </div>
        `;
        productListContainer.appendChild(productElement);
    });

    // Xử lý sự kiện click vào toàn bộ sản phẩm
    document.querySelectorAll('.product-item').forEach(item => {
        item.addEventListener('click', function (event) {
            // Nếu bấm vào vùng "Xem nhanh", không cần xử lý tiếp
            if (event.target.closest('.quick-view-btn')) return;

            const productId = this.getAttribute('data-id');
            if (productId) {
                window.location.href = `/chi-tiet-san-pham?id=${productId}`;
            }
        });
    });

    // Xử lý click riêng cho nút "Xem nhanh"
    document.querySelectorAll('.quick-view-btn').forEach(button => {
        button.addEventListener('click', function (event) {
            event.preventDefault();
            const productId = this.getAttribute('data-id');
            if (productId) {
                window.location.href = `/chi-tiet-san-pham?id=${productId}`;
            }
        });
    });
}


// Hàm cập nhật phân trang
function updatePaginationControls(totalPages) {
    const paginationContainer = document.getElementById('pagination-controls');
    paginationContainer.innerHTML = ''; // Xóa các nút cũ

    // Nút Previous
    const previousButton = document.createElement('li');
    previousButton.className = `page-item ${currentPage === 0 ? 'disabled' : ''}`;
    previousButton.innerHTML = `
        <a class="page-link" href="#" aria-label="Previous" onclick="changePage(${currentPage - 1})">
            <span aria-hidden="true">&laquo;</span>
        </a>
    `;
    paginationContainer.appendChild(previousButton);

    // Các nút số trang
    for (let i = 0; i < totalPages; i++) {
        const pageButton = document.createElement('li');
        pageButton.className = `page-item ${currentPage === i ? 'active' : ''}`;
        pageButton.innerHTML = `<a class="page-link" href="#" onclick="changePage(${i})">${i + 1}</a>`;
        paginationContainer.appendChild(pageButton);
    }

    // Nút Next
    const nextButton = document.createElement('li');
    nextButton.className = `page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`;
    nextButton.innerHTML = `
        <a class="page-link" href="#" aria-label="Next" onclick="changePage(${currentPage + 1})">
            <span aria-hidden="true">&raquo;</span>
        </a>
    `;
    paginationContainer.appendChild(nextButton);
}

// Hàm thay đổi trang khi nhấn vào nút phân trang
function changePage(page) {
    if (page < 0 || page === currentPage) return;
    currentPage = page;
    fetchProducts(page); // Gọi lại fetchProducts với trang mới
}


// Gọi hàm fetchProducts khi trang được tải
window.onload = () => fetchProducts(currentPage);
document.getElementById("searchInput").addEventListener("input", function () {
    let keyword = this.value.trim();
    if (keyword.length > 0) {
        fetch(`/api/sanpham/search?keyword=${encodeURIComponent(keyword)}`)
            .then(response => response.json())
            .then(data => {
                let resultDiv = document.getElementById("searchResults");
                // Giới hạn tối đa 3 sản phẩm
                let limitedResults = data.content.slice(0, 3);

                resultDiv.innerHTML = limitedResults.map(sp =>
                    `<div class="product-item" data-id="${sp.id}" style="cursor: pointer;">
                        <img src="${sp.urlHinhAnh}" alt="${sp.tenSanPham}" width="50">
                        <span>${sp.tenSanPham} - ${sp.gia} VND</span>
                    </div>`
                ).join("");

                // Gắn sự kiện click cho sản phẩm
                document.querySelectorAll('.product-item').forEach(item => {
                    item.addEventListener('click', function () {
                        const productId = this.getAttribute('data-id');
                        if (productId) {
                            window.location.href = `/chi-tiet-san-pham?id=${productId}`;
                        }
                    });
                });
            })
            .catch(error => console.error("Lỗi tìm kiếm:", error));
    } else {
        document.getElementById("searchResults").innerHTML = ""; // Xóa kết quả khi xóa input
    }
});
document.addEventListener("DOMContentLoaded", function () {
    fetch("http://localhost:8080/api/danh-muc")
        .then(response => response.json())
        .then(data => {
            let danhMucContainer = document.getElementById("danh-muc-list");
            danhMucContainer.innerHTML = ""; // Xóa danh mục cũ

            // Thêm mục "Tất cả"
            let tatCaElement = document.createElement("a");
            tatCaElement.href = "/san-pham";
            tatCaElement.className = "nav-item nav-link";
            tatCaElement.textContent = "Tất cả";

            tatCaElement.addEventListener("click", function (event) {
                event.preventDefault();
                sessionStorage.removeItem("selectedCategory"); // Xóa bộ lọc danh mục
                window.location.href = "/san-pham"; // Chuyển hướng để hiển thị tất cả sản phẩm
            });

            danhMucContainer.appendChild(tatCaElement); // Thêm vào danh sách danh mục

            // Thêm các danh mục từ API
            data.forEach(danhMuc => {
                let danhMucElement = document.createElement("a");
                danhMucElement.href = "/san-pham"; // Chuyển hướng tới trang sản phẩm
                danhMucElement.className = "nav-item nav-link";
                danhMucElement.textContent = danhMuc.tenDanhMuc;

                danhMucElement.addEventListener("click", function (event) {
                    event.preventDefault();
                    sessionStorage.setItem("selectedCategory", danhMuc.tenDanhMuc);
                    window.location.href = "/san-pham"; // Chuyển hướng để lọc theo danh mục
                });

                danhMucContainer.appendChild(danhMucElement);
            });
        })
        .catch(error => console.error("Lỗi tải danh mục:", error));
});

document.addEventListener("DOMContentLoaded", function () {
    let danhMuc = sessionStorage.getItem("selectedCategory");

    fetchProducts(currentPage).then(() => {
        // Sau khi fetch xong, mới xóa sessionStorage
        sessionStorage.removeItem("selectedCategory");
    });
});







