#!/usr/bin/env python
# coding=utf-8

import wx
import socket
import ConfigParser

class IPValidator(wx.PyValidator):
    def __init__(self):
        wx.PyValidator.__init__(self)

    def Clone(self):
        return IPValidator()

    def Validate(self):
        text_ctrl = self.GetWindow()

        try:
            socket.inet_aton(text_ctrl.GetValue())
            text_ctrl.SetBackgroundColour(wx.SystemSettings_GetColour(wx.SYS_COLOUR_WINDOW))
            text_ctrl.Refresh()
        except socket.error:
            wx.MessageBox("Please enter a valid IP adddress", "Error")
            text_ctrl.SetBackgroundColour("pink")
            text_ctrl.SetFocus()
            text_ctrl.Refresh()
            return False

        return True

class MainFrame(wx.Frame):
    def __init__(self):
        wx.Frame.__init__(self, None, title="DCS")

        self.Bind(wx.EVT_CLOSE, self.__save_config)

        self.__read_config()

        self.__tbicon = wx.TaskBarIcon()
        self.__tbicon.SetIcon(wx.Icon(self.__get_icon_name(), wx.BITMAP_TYPE_PNG), self.__get_tooltip())
        self.__tbicon.Bind(wx.EVT_TASKBAR_LEFT_DCLICK, self.__on_tbicon_click)
        self.__hidden = False

        panel = wx.Panel(self)

        master_ip_static_text = wx.StaticText(panel, label="Master Node IP address:")
        self.__master_ip_text_ctrl = wx.TextCtrl(panel, size=(120, 25), validator=IPValidator())
        bind_ip_static_text = wx.StaticText(panel, label="IP address to bind to (optional):")
        self.__bind_ip_text_ctrl = wx.TextCtrl(panel, size=(120, 25), validator=IPValidator())

        master_ip, bind_ip = self.__read_config()
        self.__master_ip_text_ctrl.SetValue(master_ip or "")
        self.__bind_ip_text_ctrl.SetValue(bind_ip or "")

        register_button = wx.Button(panel, label="Register")
        register_button.SetDefault()
        register_button.Bind(wx.EVT_BUTTON, self.__on_register)

        # layout starts here

        sizer_ips = wx.FlexGridSizer(2, vgap=5, hgap=5)
        sizer_ips.Add(master_ip_static_text, flag=wx.ALIGN_CENTER_VERTICAL | wx.ALIGN_RIGHT)
        sizer_ips.Add(self.__master_ip_text_ctrl, flag=wx.ALIGN_CENTER_VERTICAL)

        sizer_ips.Add(bind_ip_static_text, flag=wx.ALIGN_CENTER_VERTICAL | wx.ALIGN_RIGHT)
        sizer_ips.Add(self.__bind_ip_text_ctrl, flag=wx.ALIGN_CENTER_VERTICAL)

        sizer_button = wx.BoxSizer(wx.HORIZONTAL)
        sizer_button.Add(register_button)

        sizer = wx.BoxSizer(wx.VERTICAL)
        sizer.Add(sizer_ips, flag=wx.ALIGN_LEFT | wx.ALIGN_CENTER_HORIZONTAL | wx.ALL, border=10)
        sizer.Add(sizer_button, flag=wx.ALIGN_CENTER_HORIZONTAL | wx.ALL, border=10)

        panel.SetSizer(sizer)
        sizer.Fit(self)

        self.Show(True)

    def __on_register(self, event):
        self.__master_ip_text_ctrl.GetValidator().Validate()
        if self.__bind_ip_text_ctrl.GetValue() != "":
            self.__bind_ip_text_ctrl.GetValidator().Validate()

        # TODO register

    def __get_icon_name(self):
        suffix = ""
        if "wxMSW" in wx.PlatformInfo:
            suffix = "-16x16"
        elif "wxGTK" in wx.PlatformInfo:
            suffix = "-22x22"
        return "network-idle" + suffix + ".png"

    def __get_tooltip(self):
        return "not connected"

    __CONFIG_FILE_NAME = "dcs.cfg"
    __IPS_SECTION = "IPs"
    __MASTER_IP = "Master Node IP"
    __BIND_IP = "IP to bind to"
    def __read_config(self):
        config = ConfigParser.SafeConfigParser()
        try:
            config.read(self.__CONFIG_FILE_NAME)
            master_ip = config.get(self.__IPS_SECTION, self.__MASTER_IP)
            bind_ip = config.get(self.__IPS_SECTION, self.__BIND_IP)
            return (master_ip, bind_ip)
        except ConfigParser.Error:
            return (None, None)

    def __save_config(self, event):
        config = ConfigParser.SafeConfigParser()
        config.add_section(self.__IPS_SECTION)
        config.set(self.__IPS_SECTION, self.__MASTER_IP, self.__master_ip_text_ctrl.GetValue())
        config.set(self.__IPS_SECTION, self.__BIND_IP, self.__bind_ip_text_ctrl.GetValue())

        with open(self.__CONFIG_FILE_NAME, "wb") as configfile:
            config.write(configfile)

        event.Skip()

    def __on_tbicon_click(self, event):
        self.Show(self.__hidden)
        self.__hidden = not self.__hidden

app = wx.App()
MainFrame()
if __name__ == "__main__":
    app.MainLoop()
