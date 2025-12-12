const ordenSalidaManager = (function() {
    'use strict';

    // Configuración
    const config = {
        endpoints: {
            productos: '/ordenes-salida/productos',
            imprimir: '/ordenes-salida/imprimir',
            eliminar: '/ordenes-salida/eliminar'
        }
    };

    function init() {
        setupEventListeners();
        setCurrentPeriod();
        console.log("Módulo de Órdenes de Salida inicializado");
    }

    function setupEventListeners() {
        const btnFiltrar = document.getElementById('btn-filtrar');
        const btnLimpiar = document.getElementById('btn-limpiar');
        const busquedaInput = document.getElementById('busqueda');

        if (btnFiltrar) btnFiltrar.addEventListener('click', buscarOrdenes);
        if (btnLimpiar) btnLimpiar.addEventListener('click', limpiarBusqueda);
        if (busquedaInput) {
            busquedaInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') buscarOrdenes();
            });
        }

        const modal = document.getElementById('nuevaSalidaModal');
        if (modal) {
            modal.addEventListener('show.bs.modal', handleModalShow);
            modal.addEventListener('hidden.bs.modal', handleModalHide);
        }

        const formSalida = document.getElementById('formSalidaModal');
        if (formSalida) {
            formSalida.addEventListener('submit', validarFormulario);
        }
    }

    function setCurrentPeriod() {
        const now = new Date();
        const currentYear = now.getFullYear().toString();
        const currentMonth = (now.getMonth() + 1).toString().padStart(2, '0');

        const periodoSelect = document.getElementById('periodoSelect');
        const mesSelect = document.getElementById('mesSelect');

        if (periodoSelect) periodoSelect.value = currentYear;
        if (mesSelect) mesSelect.value = currentMonth;
    }

    function handleModalShow() {
        console.log('Modal abierto - cargando productos...');
        cargarProductos();
    }

    function handleModalHide() {
        const form = document.getElementById('formSalidaModal');
        if (form) form.reset();

        const tabla = document.getElementById('tabla-productos');
        if (tabla) tabla.innerHTML = '';

        resetProductoSeleccionado();
    }

    function buscarOrdenes() {
        const busqueda = document.getElementById('busqueda').value.trim();

        let url = `/ordenes-salida`;

        if (busqueda) {
            url += `?busqueda=${encodeURIComponent(busqueda)}`;
        }

        console.log('URL de búsqueda:', url);
        window.location.href = url;
    }

    function limpiarBusqueda() {
        const busquedaInput = document.getElementById('busqueda');
        if (busquedaInput) {
            busquedaInput.value = '';
        }
        buscarOrdenes();
    }

    function cargarProductos() {
        showLoading(true);

        fetch(config.endpoints.productos)
            .then(response => {
                if (!response.ok) throw new Error('Error al cargar productos');
                return response.json();
            })
            .then(productos => {
                mostrarProductos(productos);
                if (productos.length > 0) {
                    seleccionarPrimerProducto(productos[0]);
                }
            })
            .catch(error => {
                console.error('Error al cargar productos:', error);
                showError('Error al cargar la lista de productos: ' + error.message);
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function mostrarProductos(productos) {
        const tabla = document.getElementById('tabla-productos');
        if (!tabla) {
            console.error('No se encontró la tabla de productos');
            return;
        }

        tabla.innerHTML = '';

        if (productos.length === 0) {
            tabla.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted">
                        No hay productos disponibles
                    </td>
                </tr>
            `;
            return;
        }

        productos.forEach(producto => {
            const fila = document.createElement('tr');
            fila.innerHTML = `
                <td>
                    <input type="radio" name="productoSeleccionado" value="${producto.id}"
                           onchange="ordenSalidaManager.seleccionarProducto(${producto.id}, '${escapeHtml(producto.codigo)}', '${escapeHtml(producto.nombre)}', ${producto.cantidad})">
                </td>
                <td>${escapeHtml(producto.codigo)}</td>
                <td>${escapeHtml(producto.unidadMedida || 'N/A')}</td>
                <td>${escapeHtml(producto.nombre)}</td>
                <td>${escapeHtml(producto.categoria || 'N/A')}</td>
                <td>${producto.cantidad}</td>
            `;
            tabla.appendChild(fila);
        });
    }

    function seleccionarPrimerProducto(producto) {
        setTimeout(() => {
            seleccionarProducto(
                producto.id,
                producto.codigo,
                producto.nombre,
                producto.cantidad
            );

            const primerRadio = document.querySelector('#tabla-productos input[type="radio"]');
            if (primerRadio) {
                primerRadio.checked = true;
                primerRadio.closest('tr').classList.add('producto-seleccionado');
            }
        }, 100);
    }

    function buscarProductos() {
        const termino = document.getElementById('search-term').value.toLowerCase();
        const filas = document.querySelectorAll('#tabla-productos tr');

        filas.forEach(fila => {
            const textoFila = fila.textContent.toLowerCase();
            if (textoFila.includes(termino)) {
                fila.style.display = '';
            } else {
                fila.style.display = 'none';
            }
        });
    }

    function seleccionarProducto(id, codigo, nombre, stock) {
        document.getElementById('product-id').value = id;
        document.getElementById('product-code').value = codigo;
        document.getElementById('product-name').value = nombre;
        document.getElementById('available-stock').value = stock;

        const cantidadInput = document.getElementById('product-quantity');
        if (cantidadInput) {
            cantidadInput.disabled = false;
            cantidadInput.max = stock;
            cantidadInput.value = 1;
        }

        resaltarFilaSeleccionada();
    }

    function resaltarFilaSeleccionada() {
        const filas = document.querySelectorAll('#tabla-productos tr');
        filas.forEach(fila => {
            fila.classList.remove('producto-seleccionado');
            const radio = fila.querySelector('input[type="radio"]');
            if (radio && radio.checked) {
                fila.classList.add('producto-seleccionado');
            }
        });
    }

    function resetProductoSeleccionado() {
        document.getElementById('product-id').value = '';
        document.getElementById('product-code').value = '';
        document.getElementById('product-name').value = '';
        document.getElementById('available-stock').value = '';

        const cantidadInput = document.getElementById('product-quantity');
        if (cantidadInput) {
            cantidadInput.disabled = true;
            cantidadInput.value = '';
        }
    }

    function validarFormulario(e) {
        const productoId = document.getElementById('product-id').value;
        const cantidad = document.getElementById('product-quantity').value;
        const dniUsuario = document.getElementById('user-dni').value;

        let errores = [];

        if (!dniUsuario || !dniUsuario.match(/^\d{8}$/)) {
            errores.push('El DNI debe tener 8 dígitos numéricos');
        }

        if (!productoId) {
            errores.push('Por favor seleccione un producto');
        }

        if (!cantidad || cantidad <= 0) {
            errores.push('Por favor ingrese una cantidad válida');
        }

        const stock = parseInt(document.getElementById('available-stock').value);
        if (parseInt(cantidad) > stock) {
            errores.push('La cantidad no puede ser mayor al stock disponible');
        }

        if (errores.length > 0) {
            e.preventDefault();
            showError(errores.join('<br>'));
            return false;
        }

        return true;
    }

    function imprimirSalida(numeroOrden) {
        if (!numeroOrden) {
            alert('Error: Número de orden no válido');
            return;
        }

        console.log('Iniciando impresión para orden:', numeroOrden);

        const url = `/ordenes-salida/imprimir/${encodeURIComponent(numeroOrden)}`;
        console.log('URL de impresión:', url);

        window.location.href = url;
    }

    function editarSalida(id) {
        if (!id) {
            showError('ID de orden no válido');
            return;
        }

        console.log('Editando orden ID:', id);
        window.location.href = `/ordenes-salida/editar/${id}`;
    }

    function eliminarSalida(id, numeroOrden) {
        if (!id || !numeroOrden) {
            showError('Datos de orden no válidos');
            return;
        }

        if (confirm(`¿Está seguro que desea eliminar la orden ${numeroOrden}? Esta acción no se puede deshacer.`)) {
            fetch(`/ordenes-salida/eliminar/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la respuesta del servidor');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    showSuccess('Orden eliminada exitosamente');
                    setTimeout(() => location.reload(), 1500);
                } else {
                    showError('Error al eliminar la orden: ' + (data.message || 'Error desconocido'));
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showError('Error al eliminar la orden: ' + error.message);
            });
        }
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function showLoading(show) {
        if (show) {
            console.log('Cargando...');
        }
    }

    function showError(message) {
        if (typeof Swal !== 'undefined') {
            Swal.fire('Error', message, 'error');
        } else {
            alert('Error: ' + message);
        }
    }

    function showSuccess(message) {
        if (typeof Swal !== 'undefined') {
            Swal.fire('Éxito', message, 'success');
        } else {
            alert('Éxito: ' + message);
        }
    }

    return {
        init,
        buscarOrdenes,
        limpiarBusqueda,
        cargarProductos,
        buscarProductos,
        seleccionarProducto,
        imprimirSalida,
        editarSalida,
        eliminarSalida
    };

})();

document.addEventListener('DOMContentLoaded', function() {
    ordenSalidaManager.init();
});