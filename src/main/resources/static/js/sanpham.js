let currentPage = 0; // Trang hiện tại
const pageSize = 6;  // Số sản phẩm trên mỗi trang

// Hàm lấy dữ liệu sản phẩm từ API
async function fetchProducts(page = 0) {
    try {
        const response = await fetch(`http://localhost:8080/api/sanpham?page=${page}`);

        if (!response.ok) {
            throw new Error(`Lỗi API! Mã trạng thái: ${response.status}`);
        }

        const data = await response.json();

        if (!data || !data.content) {
            throw new Error('API không trả về dữ liệu hợp lệ');
        }

        renderProducts(data.content); // Hiển thị sản phẩm
        updatePaginationControls(data.totalPages); // Cập nhật phân trang
    } catch (error) {
        console.error('Lỗi khi tải sản phẩm:', error);
    }
}

// Hàm hiển thị danh sách sản phẩm
function renderProducts(products) {
    const productListContainer = document.getElementById('product-list');
    productListContainer.innerHTML = ''; // Xóa sản phẩm cũ

    products.forEach(product => {
        const productElement = document.createElement('div');
        productElement.classList.add('col-lg-4', 'col-md-6', 'col-sm-12', 'pb-1');
        productElement.innerHTML = `
            <div class="card product-item border-0 mb-4">
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
                    <a href="#" class="btn btn-sm text-dark p-0 quick-view-btn" data-id="${product.id}">
                        <i class="fas fa-eye text-primary mr-1"></i>Xem nhanh
                    </a>
                    <a href="#" class="btn btn-sm text-dark p-0"><i class="fas fa-shopping-cart text-primary mr-1"></i>Thêm vào giỏ hàng</a>
                </div>
            </div>
        `;
        productListContainer.appendChild(productElement);
    });
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
    fetchProducts(page);
}

// Gọi hàm fetchProducts khi trang được tải
window.onload = () => fetchProducts(currentPage);
