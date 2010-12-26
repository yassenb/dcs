#!/usr/bin/env python
# coding=utf-8

import wx
import socket
import httplib
import urllib
import ConfigParser
import os
import re

class IPValidator(wx.PyValidator):
    def __init__(self, allow_hostname):
        wx.PyValidator.__init__(self)
        self.__allow_hostname = allow_hostname

    def Clone(self):
        return IPValidator(self.__allow_hostname)

    def Validate(self):
        text_ctrl = self.GetWindow()

        try:
            address = text_ctrl.GetValue()
            socket.inet_aton(address)
        except socket.error:
            if not (self.__allow_hostname and IPValidator.__is_valid_hostname(address)):
                error_msg = "Please enter a valid IP adddress"
                if self.__allow_hostname:
                    error_msg += " or hostname"
                wx.MessageBox(error_msg, "Error")

                text_ctrl.SetBackgroundColour("pink")
                text_ctrl.SetFocus()
                text_ctrl.Refresh()
                return False

        text_ctrl.SetBackgroundColour(wx.SystemSettings_GetColour(wx.SYS_COLOUR_WINDOW))
        text_ctrl.Refresh()
        return True

    # copied from stackoverflow.com
    @staticmethod
    def __is_valid_hostname(hostname):
        if len(hostname) > 255:
            return False
        if hostname[-1:] == ".":
            hostname = hostname[:-1] # strip exactly one dot from the right, if present
        allowed = re.compile("(?!-)[A-Z\d-]{1,63}(?<!-)$", re.IGNORECASE)
        return all(allowed.match(x) for x in hostname.split("."))

class MainFrame(wx.Frame):
    def __init__(self):
        wx.Frame.__init__(self, None, title="DCS")

        self.__base_path = os.path.dirname(__file__) + "/"
        master_ip, bind_ip, self.__registered = self.__read_config()

        self.Bind(wx.EVT_CLOSE, self.__save_config)

        self.__tbicon = wx.TaskBarIcon()
        self.__tbicon.SetIcon(wx.Icon(self.__get_icon_name(), wx.BITMAP_TYPE_PNG), self.__get_tooltip())
        self.__tbicon.Bind(wx.EVT_TASKBAR_LEFT_DCLICK, self.__on_tbicon_click)

        self.Show(not self.__registered)

        panel = wx.Panel(self)

        master_ip_static_text = wx.StaticText(panel, label="Master Node IP address or hostname:")
        self.__master_ip_text_ctrl = wx.TextCtrl(panel, size=(250, 25), validator=IPValidator(True))
        bind_ip_static_text = wx.StaticText(panel, label="IP address to bind to (optional):")
        self.__bind_ip_text_ctrl = wx.TextCtrl(panel, size=(120, 25), validator=IPValidator(False))

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

    def __on_register(self, event):
        self.__registered = False
        if not self.__master_ip_text_ctrl.GetValidator().Validate() or \
           (self.__bind_ip_text_ctrl.GetValue() != "" and not self.__bind_ip_text_ctrl.GetValidator().Validate()):
               return
        if not self.__act_on_client({"action": "register"}):
            wx.MessageBox("Problem registering with given Master Node", "Error")
        else:
            self.__registered = True
            wx.MessageBox("Successfully registered with Master Node")

    def __act_on_client(self, params):
        master_ip = self.__master_ip_text_ctrl.GetValue()
        bind_ip = self.__bind_ip_text_ctrl.GetValue()
        if bind_ip:
            conn = httplib.HTTPConnection(master_ip, source_address=bind_ip)
        else:
            conn = httplib.HTTPConnection(master_ip)

        params = urllib.urlencode(params)
        try:
            conn.request("GET", "/cgi-bin/action.py?" + params)
            response = conn.getresponse()
            conn.close()
            return response.status == 200
        except:
            return False

    def __get_icon_name(self):
        suffix = ""
        if "wxMSW" in wx.PlatformInfo:
            suffix = "-16x16"
        elif "wxGTK" in wx.PlatformInfo:
            suffix = "-22x22"
        return self.__base_path + "icons/" + "network-idle" + suffix + ".png"

    def __get_tooltip(self):
        return "not connected"

    __CONFIG_FILE_NAME = "dcs.cfg"
    __IPS_SECTION = "IPs"
    __MASTER_IP = "Master Node IP"
    __BIND_IP = "IP to bind to"
    __REGISTERED = "Successfully registered"
    def __read_config(self):
        config = ConfigParser.SafeConfigParser()
        try:
            config.read(self.__base_path + self.__CONFIG_FILE_NAME)
            master_ip = config.get(self.__IPS_SECTION, self.__MASTER_IP)
            bind_ip = config.get(self.__IPS_SECTION, self.__BIND_IP)
            registered = config.getboolean(self.__IPS_SECTION, self.__REGISTERED)
            return (master_ip, bind_ip, registered)
        except ConfigParser.Error:
            return (None, None, None)

    def __save_config(self, event):
        config = ConfigParser.SafeConfigParser()
        config.add_section(self.__IPS_SECTION)
        config.set(self.__IPS_SECTION, self.__MASTER_IP, self.__master_ip_text_ctrl.GetValue())
        config.set(self.__IPS_SECTION, self.__BIND_IP, self.__bind_ip_text_ctrl.GetValue())
        config.set(self.__IPS_SECTION, self.__REGISTERED, str(self.__registered))

        with open(self.__base_path + self.__CONFIG_FILE_NAME, "wb") as configfile:
            config.write(configfile)

        event.Skip()

    def __on_tbicon_click(self, event):
        self.Show(not self.IsShown())

app = wx.App()
MainFrame()
if __name__ == "__main__":
    app.MainLoop()
