# Copyright 2012 Steven Watanabe
# Distributed under the Boost Software License, Version 1.0.
# (See accompanying file LICENSE_1_0.txt or http://www.boost.org/LICENSE_1_0.txt)

import os ;
import targets ;
import project ;
import "class" : new ;
import virtual-target ;
import configure ;
import path ;
import property ;
import property-set ;
import common ;


rule get-root-project ( project )
{
    # Find the root project.
    local root-project = $(project) ;
    root-project = [ $(root-project).project-module ] ;
    while
        [ project.attribute $(root-project) parent-module ] &&
        [ project.attribute $(root-project) parent-module ] != user-config &&
        [ project.attribute $(root-project) parent-module ] != project-config
    {
        root-project = [ project.attribute $(root-project) parent-module ] ;
    }
    return $(root-project) ;
}

TOUCH = [ common.file-touch-command ] ;

actions touch {
   $(TOUCH) "$(<)"
}

rule can-symlink ( project : ps )
{
    if ! $(.can-symlink)
    {
        local root-project = [ get-root-project $(project) ] ;

        local source-target = [ new file-target test-symlink-source : :
            $(project) : [ new action : link.touch ] ] ;
        local target = [ new file-target test-symlink : :
            $(project) : [ new action $(source-target) : link.mklink ] ] ;

        if [ configure.try-build $(target) : $(ps) : "symlinks supported" ]
        {
            .can-symlink = true ;
        }       
        else
        {
            .can-symlink = false ;
        }
    }
    if $(.can-symlink) = true
    {
        return true ;
    }
}

if [ os.name ] = NT
{

# Test for Windows junctions (mklink /J)
rule can-junction ( project : ps )
{
    if ! $(.can-junction)
    {
        local root-project = [ get-root-project $(project) ] ;

        local source-target = [ new file-target test-junction-source : :
            $(project) : [ new action : common.mkdir ] ] ;
        local target = [ new file-target test-junction : :
            $(project) : [ new action $(source-target) : link.junction ] ] ;

        if [ configure.try-build $(target) : $(ps) : "junctions supported" ]
        {
            .can-junction = true ;
        }       
        else
        {
            .can-junction = false ;
        }
    }
    if $(.can-junction) = true
    {
        return true ;
    }
}

}
else
{

rule can-junction ( project : ps )
{
}

}

rule can-hardlink ( project : ps )
{
    if ! $(.can-hardlink)
    {
        local root-project = [ get-root-project $(project) ] ;

        local source-target = [ new file-target test-hardlink-source : :
            $(project) : [ new action : link.touch ] ] ;
        # Use <location-prefix> so that the destination link is created
        # in a different directory. AFS refuses to make hard links
        # between files in different directories, so we want to check
        # it.
        local target = [ new file-target test-hardlink : :
            $(project) : [ new action $(source-target) : link.hardlink
            : [ new property-set <location-prefix>symlink ]
            ] ] ;

        if [ configure.try-build $(target) : $(ps) : "hardlinks supported" ]
        {
            .can-hardlink = true ;
        }       
        else
        {
            .can-hardlink = false ;
        }
    }
    if $(.can-hardlink) = true
    {
        return true ;
    }
}

class file-or-directory-reference : basic-target
{
    import virtual-target ;
    import property-set ;
    import path ;

    rule construct ( name : source-targets * : property-set )
    {
        return [ property-set.empty ] [ virtual-target.from-file $(self.name) :
            [ location ] : $(self.project) ] ;
    }

    # Returns true if the referred file really exists.
    rule exists ( )
    {
        location ;
        return $(self.file-path) ;
    }

    # Returns the location of target. Needed by 'testing.jam'.
    rule location ( )
    {
        if ! $(self.file-location)
        {
            local source-location = [ $(self.project).get source-location ] ;
            for local src-dir in $(source-location)
            {
                if ! $(self.file-location)
                {
                    local location = [ path.root $(self.name) $(src-dir) ] ;
                    if [ path.exists [ path.native $(location) ] ]
                    {
                         self.file-location = $(src-dir) ;
                         self.file-path = $(location) ;
                    }
                }
            }
        }
        return $(self.file-location) ;
    }
}

class symlink-target-class : basic-target
{
    import path ;
    import virtual-target ;
    import link ;
    import os ;
    import type ;
    rule construct ( name : source-target : property-set )
    {
        local location = [ path.join
            [ $(source-target).path ] [ $(source-target).name ] ] ;
        local files = [ path.glob-tree $(location) : * ] ;
        local targets ;

        # If we have symlinks, don't bother checking
        # for hardlinks and junctions.
        if ! [ link.can-symlink $(self.project) : $(property-set) ]
        {
            link.can-junction $(self.project) : $(property-set) ;
            link.can-hardlink $(self.project) : $(property-set) ;
        }

        if [ $(property-set).get <location> ]
        {
            property-set = [ property-set.create
                [ property.select <location> : [ $(property-set).raw ] ] ] ;
        }
        else
        {
            local path,relative-to-build-dir = [ $(property-set).target-path ] ;
            local path = $(path,relative-to-build-dir[1]) ;
            local relative-to-build-dir = $(path,relative-to-build-dir[2]) ;

            if $(relative-to-build-dir)
            {
                path = [ path.join [ $(self.project).build-dir ] $(path) ] ;
            }

            property-set = [ property-set.create <location>$(path) ] ;
        }
        
        local a = [ new non-scanning-action $(source-target) :
            link.do-link-recursively : $(property-set) ] ;
        
        local t = [ new notfile-target $(name)
            : $(self.project) : $(a) ] ;

        return [ property-set.empty ] [ virtual-target.register $(t) ] ;
    }
}

rule do-file-link
{
    local target = [ path.native [ path.relative-to [ path.pwd ] $(<) ] ] ;
    local source = [ path.native [ path.relative-to [ path.pwd ] $(>) ] ] ;
    LOCATE on $(target) = . ;
    DEPENDS $(.current-target) : $(target) ;
    if $(.can-symlink) = true
    {
        link.mklink $(target) : $(source) ;
    }
    else if $(.can-hardlink) = true
    {
        DEPENDS $(target) : $(source) ;
        link.hardlink $(target) : $(source) ;
    }
    else
    {
        DEPENDS $(target) : $(source) ;
        common.copy $(target) : $(source) ;
    }
}

rule do-link
{
    local target = [ path.native [ path.relative-to [ path.pwd ] $(<) ] ] ;
    local source = [ path.native [ path.relative-to [ path.pwd ] $(>) ] ] ;
    local relative = [ path.native [ path.relative-to [ path.parent $(<) ] $(>) ] ] ;
    if ! [ on $(target) return $(MKLINK_OR_DIR) ]
    {
        LOCATE on $(target) = . ;
        DEPENDS $(.current-target) : $(target) ;
        mklink-or-dir $(target) : $(source) ;
    }
    if [ os.name ] = NT
    {
        if $(.can-symlink) = true
        {
            MKLINK_OR_DIR on $(target) = mklink /D \"$(target)\" \"$(relative)\" ;
        }
        else
        {
            # This function should only be called
            # if either symlinks or junctions are supported.
            # To get here $(.can-junction) must be true.
            mklink-opt = /J ;
            MKLINK_OR_DIR on $(target) = mklink /J \"$(target)\" \"$(source)\" ;
        }
    }
    else
    {
        MKLINK_OR_DIR on $(target) = ln -s $(relative) $(target)  ;
    }
}

rule do-split
{
    local target = [ path.native [ path.relative-to [ path.pwd ] $(<) ] ] ;
    if ! [ on $(target) return $(MKLINK_OR_DIR) ]
    {
        LOCATE on $(target) = . ;
        DEPENDS $(.current-target) : $(target) ;
        common.mkdir $(target) ;
    }
    MKLINK_OR_DIR on $(target) = mkdir \"$(target)\" ;
}

rule do-rm
{
    local target = [ path.native [ path.relative-to [ path.pwd ] $(<) ] ] ;
    ALWAYS $(target) ;
    RM on $(target) = rmdir ;
    link.rm $(target) ;
}

rule mklink-or-dir
{
    NOUPDATE $(<) ;
}

actions mklink-or-dir
{
    $(MKLINK_OR_DIR)
}

rule link-entries ( target : files * : split ? )
{
    for local s in $(files)
    {
        local t = [ path.join $(target) [ path.basename $(s) ] ] ;
        if ! $(.known-dirs.$(t))
        {
            local t = [ path.native [ path.relative-to [ path.pwd ] $(t) ] ] ;
            local s = [ path.native [ path.relative-to [ path.pwd ] $(target) ] ] ;
            LOCATE on $(t) = . ;
            DEPENDS $(t) : $(s) ;
            NOUPDATE $(s) ;
        }
        if $(split)
        {
            link-recursively $(t) : $(s) ;
        }
        else
        {
            link-entries $(t) : [ path.glob $(s) : * ] ;
        }
    }
    if ! $(.known-dirs.$(target))
    {
        .known-dirs.$(target) += $(files) ;
        .known-dirs.base.$(target) = $(.current-target) ;
    }
}

rule link-recursively ( target : source : no-recurse ? )
{
    local split ;
    if [ CHECK_IF_FILE [ path.native $(source) ] ]
    {
        do-file-link $(target) : $(source) ;
    }
    else if $(.known-dirs.$(target)) && ! $(no-recurse)
    {
        split = true ;
        if ! $(.split-dirs.$(target))
        {
            local .current-target = $(.known-dirs.base.$(target)) ;
            for local s in $(.known-dirs.$(target))
            {
                local t = [ path.join $(target) [ path.basename $(s) ] ] ;
                link-recursively $(t) : $(s) : flat ;
            }
            if [ READLINK [ path.native $(target) ] ]
            {
                do-rm $(target) ;
            }
            do-split $(target) ;
            .split-dirs.$(target) = true ;
        }
    }
    else if [ path.exists [ path.native $(target) ] ]
    {
        local link-target = [ READLINK [ path.native $(target) ] ] ;
        if $(link-target)
        {
            local full-path =
                [ path.root [ path.make $(link-target) ] [ path.parent $(target) ] ] ;
            if $(full-path) != $(source)
            {
                do-rm $(target) ;
                do-split $(target) ;
                split = true ;
            }
        }
        else
        {
            do-split $(target) ;
            split = true ;
        }
    }
    else if $(.can-symlink) = false && $(.can-junction) = false
    {
        if [ READLINK [ path.native $(target) ] ]
        {
            do-rm $(target) ;
        }
        do-split $(target) ;
        split = true ;
    }
    else
    {
        do-link $(target) : $(source) ;
    }

    if ! $(no-recurse)
    {
        link-entries $(target) : [ path.glob $(source) : * ] : $(split) ;
    }
}

rule do-link-recursively ( target : source : properties * )
{
    local target-path = [ property.select <location> : $(properties) ] ;
    local source-path = [ on $(source) return $(LOCATE) ] [ on $(source) return $(SEARCH) ] ;

    local absolute-target = [ path.root
        [ path.join [ path.make $(target-path[1]:G=) ]
                    [ path.basename [ path.make $(source:G=) ] ] ]
        [ path.pwd ] ] ;

    local absolute-source = [ path.root
        [ path.root [ path.make $(source:G=) ]
                    [ path.make $(source-path[1]) ] ]
        [ path.pwd ] ] ;

    local .current-target = $(target) ;

    link-recursively $(absolute-target) : $(absolute-source) ;
}

rule mklink
{
    local target-path = [ on $(<) return $(LOCATE) ] [ on $(<) return $(SEARCH) ] . ;
    local source-path = [ on $(>) return $(LOCATE) ] [ on $(>) return $(SEARCH) ] . ;
    local relative-path = [ path.relative-to
        [ path.parent [ path.join [ path.root [ path.make $(target-path[1]) ] [ path.pwd ] ] [ path.make $(<:G=) ] ] ]
        [ path.join [ path.root [ path.make $(source-path[1]) ] [ path.pwd ] ] [ path.make $(>:G=) ] ] ] ;

    PATH_TO_SOURCE on $(<) = [ path.native $(relative-path) ] ;
    NOUPDATE $(<) ;
}

if [ os.name ] = NT
{

actions junction
{
    if exist "$(<)" del "$(<)"
    mklink /J "$(<)" "$(>)"
}

actions mklink
{
    if exist "$(<)" del "$(<)"
    mklink "$(<)" "$(PATH_TO_SOURCE)"
}

actions hardlink
{
    if exist "$(<)" del "$(<)"
    mklink /H "$(<)" "$(>)"
}

actions rm
{
    rmdir "$(<)"
}

}
else
{

actions mklink
{
    ln -f -s "$(PATH_TO_SOURCE)" "$(<)"
}

actions hardlink
{
    ln -f "$(>)" "$(<)"
}

actions rm
{
    rm "$(<)"
}

}

rule link-directory ( name : sources : requirements * : default-build * : usage-requirements * )
{
    local project = [ project.current ] ;
    sources = [ new file-or-directory-reference $(sources) : $(project) ] ;
    targets.main-target-alternative $(sources) ;
    return [ targets.main-target-alternative
        [ new symlink-target-class $(name) : $(project)
            : [ targets.main-target-sources $(sources) : $(name) : no-renaming ]
            : [ targets.main-target-requirements $(requirements) : $(project) ]
            : [ targets.main-target-default-build : $(project) ]
            : [ targets.main-target-usage-requirements $(usage-requirements) :
                $(project) ] ] ] ;
}

IMPORT $(__name__) : link-directory : : link-directory ;
