sap.ui.define([
   "sap/ui/core/mvc/Controller"
], function (Controller) {
   "use strict";
   return Controller.extend("blueprint.controller.TodoList", {
	   onItemPress : function (oEvent) {
		   this.getOwnerComponent().getRouter().navTo("Todos", {
			    "id" : oEvent.getParameter("listItem").getBindingContext().getProperty("Id")
		   });
	   },
	   onDelete : function (oEvent) {
		   var list = this.byId("list");
		   var dataModel = this.getView().getModel();
		   list.getSelectedContexts().forEach(function (c) {
			   dataModel.remove("/Todos('"+c.getProperty("Id")+"')");
		   });
	   },
	   onAdd : function (oEvent) {
		   this.getOwnerComponent().getRouter().navTo("addTodo");
	   }
   });
});