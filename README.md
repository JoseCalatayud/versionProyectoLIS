Descripción del problema
Un pequeño negocio necesita una aplicación web para gestionar su inventario de artículos. La aplicación debe permitir registrar y mantener los artículos disponibles, gestionar compras y ventas de artículos, y controlar el acceso a las funcionalidades según el rol de usuario.

Requisitos de la aplicación
La aplicación debe incluir las siguientes funcionalidades:

Gestión de artículos:
		El sistema debe estar preparado para permitir dar de alta nuevos artículos en el sistema. Al dar de alta un artículo, debemos ser capaces de almacenar el nombre del artículo, su descripción, código de barras, familia o categoría, fotografía (opcional), precio de venta y stock inicial que tenemos de dicho artículo. La aplicación debe asegurarse de que no existan dos artículos con el mismo código de barras en el sistema. En caso de querer dar de alta un artículo ya existente, se produce un erro que impide dar de alta el artículo. 
		Así mismo, el sistema permitirá modificar la información de un artículo existente, siempre que no se trate de su código de barras. Para ello, el usuario seleccionará el artículo que quiere modificar, la aplicación cargará la ficha del artículo que tiene almacenada y permitirá los cambios del usuario. Cuando el usuario confirme los cambios, estos quedarán almacenados en el sistema. 
		Del mismo modo que se puede modificar un artículo, también se podrá borrar un artículo. Sin embargo, el borrado tiene sus peculiaridades, ya que no queremos perder los movimientos de compra/venta históricos de dicho artículo. Así mismo, un artículo “borrado” podrá seguir vendiéndose mientras sigamos  teniendo stock, pero no podrá ser comprado más veces.
Gestión de ventas:
El sistema dispondrá de una funcionalidad para facilitar la venta de artículos en formato multilínea (varios artículos en una misma venta).  Para ello el usuario indicará el artículo que quiere vender mediante su código de barras, así como la cantidad a vender. El precio de venta se obtendrá de la ficha del artículo. Antes de confirmar una venta, se debe verificar si hay stock suficiente para cada artículo. Si la venta es posible, el sistema descontará la cantidad vendida del stock y registrará la operación. En caso de que alguna línea de la venta no pueda suministrarse, el sistema avisará que no se puede tramitar la venta y dará la opción al usuario de corregir las líneas erróneas.
Gestión de compras:
La aplicación soportará también la posibilidad de comprar nuevas unidades de un artículo. Para ellos, se dispondrá de un sistema donde se indicarán los artículos que queremos comprar, la cantidad que queremos comprar y el precio de compra unitario. Al igual de las ventas, el registro de nuevas compras de artículos se hará en formato multilínea, permitiendo registrar la compra de múltiples artículos al mismo tiempo. Al confirmar una compra, se registra el movimiento de compra y la cantidad adquirida debe sumarse al stock del artículo correspondiente.

Control de acceso por roles:
Existen dos tipos de usuarios en el sistema:
Usuario administrador: Puede gestionar artículos (crear nuevos artículos, modificarlos y borrarlos) y registrar compras y ventas.
Usuario estándar: Solo puede realizar ventas.
En función del usuario que se loguee y su rol, la aplicación final mostrará solo las opciones a las que el rol tiene acceso.
Objetivos

Diseñar la aplicación web en su totalidad: 
Diseño del modelo de datos que soporte el negocio.
Diseño de los endpoints de servicio necesarios para comunicar las capas de negocio y de presentación.
Diseño de la interfaz de usuario (pantallas de la app)

Implementar el diseño anterior.





Parte avanzada (opcional, sólo para valientes)
El cliente nos pide la posibilidad de tener un pequeño sistema de business Intelligence, teniendo la posibilidad de ver un cuadro de mando  que le permita ver, de los distintos productos:
el stock actual de productos, las ventas y compras totales (en unidades y en euros) y la evolución de las ventas y del stock a lo largo del tiempo.

Consejo: A la hora de diseñar el sistema, sobre todo a nivel del modelo de datos, tener esta parte presente desde el comienzo (si consideráis que la vais a abordar) puede ser interesante para no tener que rehacer partes a posteriori.
